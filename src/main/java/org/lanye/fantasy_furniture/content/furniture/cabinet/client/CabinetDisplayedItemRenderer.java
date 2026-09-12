package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetYaw;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.geolib.GeolibBlockItem;

/**
 * Cabinet displayed items: shelf floor origin then offset then scale.
 *
 * <p>MODEL blocks scale from occupancy (VoxelShape), floor-aligned with model AABB.
 * Doors / tall plants render both halves so the miniature keeps full-item proportions.
 * Beds use FIXED + BEWLR with full head+foot bounds (not foot-only occupancy).
 * Only {@link GeolibBlockItem} uses GEO_FIXED; other ENTITYBLOCK_ANIMATED (chests etc.)
 * scale from occupancy and floor-align with FIXED afterDisplay.
 *
 * <p>When a supporting shelf is missing, floor Y and fit height come from
 * {@link CabinetStackLayout} so items in the same column stack flush.
 */
@OnlyIn(Dist.CLIENT)
final class CabinetDisplayedItemRenderer {

    /**
     * FIXED + Geo item static offset relative to corner collision box.
     *
     * <p>{@code ItemRenderer} applies {@code translate(-0.5)}; {@code GeoItemRenderer#preRender}
     * adds {@code +(0.5, 0.51, 0.5)} so net {@code (0, 0.01, 0)} on local geo, equivalent to
     * {@code move(-0.5, 0.01, -0.5)} at the corner.
     */
    private static final double GEO_FIXED_DX = -0.5;
    private static final double GEO_FIXED_DY = 0.01;
    private static final double GEO_FIXED_DZ = -0.5;

    private static final AABB UNIT = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    /**
     * Bed BEWLR (no level): head at z=0 + foot at z=-1 after {@code BedRenderer#renderPiece}.
     * Passed through FIXED + (-0.5) like {@link ItemRenderer}.
     */
    private static final AABB BED_ITEM_BOUNDS = new AABB(0.0, 0.1875, -1.0, 1.0, 0.5625, 1.0);

    private CabinetDisplayedItemRenderer() {}

    static void draw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            CabinetBlockEntity be,
            int slot,
            int itemYawSteps) {
        CabinetKind kind = be.kind();
        CabinetKind.CavityFit cavity = CabinetStackLayout.cavityFitStacked(be, slot);
        float fitW = cavity.width();
        float fitH = cavity.height();
        float fitD = cavity.depth();
        float floorY = CabinetStackLayout.floorY(be, slot);

        poseStack.pushPose();
        poseStack.translate(kind.itemX(slot), floorY, kind.itemZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(CabinetYaw.degrees(itemYawSteps)));

        if (CabinetDisplayEntities.tryDraw(poseStack, bufferSource, light, stack, level, fitW, fitH, fitD)) {
            poseStack.popPose();
            return;
        }
        if (tryDrawCompositeBlockModel(poseStack, bufferSource, light, stack, fitW, fitH, fitD)) {
            poseStack.popPose();
            return;
        }
        if (tryDrawBlockModel(poseStack, bufferSource, light, stack, fitW, fitH, fitD)) {
            poseStack.popPose();
            return;
        }
        if (tryDrawAnimatedOrGeoBlockItem(poseStack, bufferSource, light, stack, level, fitW, fitH, fitD)) {
            poseStack.popPose();
            return;
        }

        drawFixedItem(poseStack, bufferSource, light, stack, level, fitW, fitH, fitD);
        poseStack.popPose();
    }

    /**
     * Scaled content height after the same AABB/scale path as {@link #draw}, for stack floors.
     * Caller already chose stacked fit W/H/D.
     */
    static float measureRenderedHeight(ItemStack stack, Level level, float fitW, float fitH, float fitD) {
        return measureRenderedSize(stack, level, fitW, fitH, fitD).height();
    }

    /** Scaled W/H/D after the same AABB/scale path as {@link #draw}. */
    static CabinetItemPicks.Size measureRenderedSize(
            ItemStack stack, Level level, float fitW, float fitH, float fitD) {
        CabinetItemPicks.Size entity = CabinetDisplayEntities.measureRenderedSize(stack, level, fitW, fitH, fitD);
        if (entity != null) {
            return entity;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            CabinetItemPicks.Size composite = measureCompositeBlockSize(blockItem, fitW, fitH, fitD);
            if (composite != null) {
                return composite;
            }
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = dispatcher.getBlockModel(state);
                AABB modelBounds = CabinetModelBounds.fromModel(model, state);
                AABB occupancy = occupancyBounds(state);
                AABB scaleBounds = maxExtentBounds(occupancy, modelBounds);
                float scale = CabinetModelBounds.scaleToFit3D(scaleBounds, fitW, fitH, fitD);
                return sizeOf(modelBounds, scale);
            }
            if (state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
                BlockState facing = northFacingItemState(state);
                if (stack.getItem() instanceof GeolibBlockItem) {
                    AABB blockBounds = occupancyBounds(facing);
                    float scale = CabinetModelBounds.scaleToFit3D(blockBounds, fitW, fitH, fitD);
                    return sizeOf(blockBounds, scale);
                }
                if (blockItem.getBlock() instanceof BedBlock) {
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    BakedModel model = itemRenderer.getModel(stack, level, null, 0);
                    AABB afterFixed =
                            CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED, BED_ITEM_BOUNDS);
                    float scale = CabinetModelBounds.scaleToFit3D(afterFixed, fitW, fitH, fitD);
                    return sizeOf(afterFixed, scale);
                }
                AABB occupancy = occupancyBounds(facing);
                float scale = CabinetModelBounds.scaleToFit3D(occupancy, fitW, fitH, fitD);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                BakedModel model = itemRenderer.getModel(stack, level, null, 0);
                AABB afterFixed = CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED);
                return sizeOf(afterFixed, scale);
            }
        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        AABB afterFixed = CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED);
        float scale = CabinetModelBounds.scaleToFit3D(afterFixed, fitW, fitH, fitD);
        return sizeOf(afterFixed, scale);
    }

    private static CabinetItemPicks.Size sizeOf(AABB bounds, float scale) {
        return new CabinetItemPicks.Size(
                (float) bounds.getXsize() * scale,
                (float) bounds.getYsize() * scale,
                (float) bounds.getZsize() * scale);
    }

    private static boolean tryDrawBlockModel(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            float fitW,
            float fitH,
            float fitD) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        if (isCompositeBlockItem(blockItem)) {
            return false;
        }
        BlockState state = blockItem.getBlock().defaultBlockState();
        if (state.getRenderShape() != RenderShape.MODEL) {
            return false;
        }
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        AABB modelBounds = CabinetModelBounds.fromModel(model, state);
        // Scale from block occupancy (same world-size blocks -> same cabinet scale as grass).
        // Max with baked-quad extents so we never underestimate size and overscale.
        AABB occupancy = occupancyBounds(state);
        AABB scaleBounds = maxExtentBounds(occupancy, modelBounds);
        float scale = CabinetModelBounds.scaleToFit3D(scaleBounds, fitW, fitH, fitD);
        // Floor-align with the AABB used by renderSingleBlock (model space).
        CabinetModelBounds.translateBlockToFloorCentered(poseStack, modelBounds, scale);
        dispatcher.renderSingleBlock(state, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
        return true;
    }

    /**
     * Geo block items: GEO_FIXED + collision-box scale. Other ENTITYBLOCK_ANIMATED (chests etc.)
     * scale from occupancy, floor-align with FIXED afterDisplay, then ItemRenderer FIXED.
     */
    private static boolean tryDrawAnimatedOrGeoBlockItem(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            float fitW,
            float fitH,
            float fitD) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        BlockState state = northFacingItemState(blockItem.getBlock().defaultBlockState());
        if (state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) {
            return false;
        }

        if (stack.getItem() instanceof GeolibBlockItem) {
            return drawGeoBlockItem(poseStack, bufferSource, light, stack, level, state, fitW, fitH, fitD);
        }
        return drawAnimatedBlockItemFixed(poseStack, bufferSource, light, stack, level, state, fitW, fitH, fitD);
    }

    private static boolean drawGeoBlockItem(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            BlockState state,
            float fitW,
            float fitH,
            float fitD) {
        AABB blockBounds = occupancyBounds(state);
        AABB renderedBounds = blockBounds.move(GEO_FIXED_DX, GEO_FIXED_DY, GEO_FIXED_DZ);
        float scale = CabinetModelBounds.scaleToFit3D(blockBounds, fitW, fitH, fitD);
        CabinetModelBounds.translateToFloorCentered(poseStack, renderedBounds, scale);
        renderFixed(poseStack, bufferSource, light, stack, level);
        return true;
    }

    private static boolean drawAnimatedBlockItemFixed(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            BlockState state,
            float fitW,
            float fitH,
            float fitD) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        // Beds: BEWLR draws head+foot; scale from full item bounds, not foot-only occupancy.
        if (state.getBlock() instanceof BedBlock) {
            AABB afterFixed =
                    CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED, BED_ITEM_BOUNDS);
            float scale = CabinetModelBounds.scaleToFit3D(afterFixed, fitW, fitH, fitD);
            CabinetModelBounds.translateToFloorCentered(poseStack, afterFixed, scale);
            renderFixed(poseStack, bufferSource, light, stack, level);
            return true;
        }
        AABB occupancy = occupancyBounds(state);
        float scale = CabinetModelBounds.scaleToFit3D(occupancy, fitW, fitH, fitD);
        AABB afterFixed = CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED);
        CabinetModelBounds.translateToFloorCentered(poseStack, afterFixed, scale);
        renderFixed(poseStack, bufferSource, light, stack, level);
        return true;
    }

    /**
     * Doors / double plants: default state is only the lower half, which looks like a squat
     * flat panel. Draw both halves and scale from the combined 1x2 footprint.
     */
    private static boolean tryDrawCompositeBlockModel(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            float fitW,
            float fitH,
            float fitD) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !isCompositeBlockItem(blockItem)) {
            return false;
        }
        BlockState lower = compositeLowerState(blockItem);
        BlockState upper = lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel lowerModel = dispatcher.getBlockModel(lower);
        BakedModel upperModel = dispatcher.getBlockModel(upper);
        AABB lowerBounds = CabinetModelBounds.fromModel(lowerModel, lower);
        AABB upperBounds = CabinetModelBounds.fromModel(upperModel, upper).move(0.0, 1.0, 0.0);
        AABB modelBounds = encompass(lowerBounds, upperBounds);
        AABB occupancy = encompass(occupancyBounds(lower), occupancyBounds(upper).move(0.0, 1.0, 0.0));
        AABB scaleBounds = maxExtentBounds(occupancy, modelBounds);
        float scale = CabinetModelBounds.scaleToFit3D(scaleBounds, fitW, fitH, fitD);
        CabinetModelBounds.translateBlockToFloorCentered(poseStack, modelBounds, scale);
        dispatcher.renderSingleBlock(lower, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
        poseStack.pushPose();
        poseStack.translate(0.0, 1.0, 0.0);
        dispatcher.renderSingleBlock(upper, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        return true;
    }

    @javax.annotation.Nullable
    private static CabinetItemPicks.Size measureCompositeBlockSize(
            BlockItem blockItem, float fitW, float fitH, float fitD) {
        if (!isCompositeBlockItem(blockItem)) {
            return null;
        }
        BlockState lower = compositeLowerState(blockItem);
        BlockState upper = lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel lowerModel = dispatcher.getBlockModel(lower);
        BakedModel upperModel = dispatcher.getBlockModel(upper);
        AABB lowerBounds = CabinetModelBounds.fromModel(lowerModel, lower);
        AABB upperBounds = CabinetModelBounds.fromModel(upperModel, upper).move(0.0, 1.0, 0.0);
        AABB modelBounds = encompass(lowerBounds, upperBounds);
        AABB occupancy = encompass(occupancyBounds(lower), occupancyBounds(upper).move(0.0, 1.0, 0.0));
        AABB scaleBounds = maxExtentBounds(occupancy, modelBounds);
        float scale = CabinetModelBounds.scaleToFit3D(scaleBounds, fitW, fitH, fitD);
        return sizeOf(modelBounds, scale);
    }

    private static boolean isCompositeBlockItem(BlockItem blockItem) {
        return blockItem.getBlock() instanceof DoorBlock
                || blockItem.getBlock() instanceof DoublePlantBlock;
    }

    private static BlockState compositeLowerState(BlockItem blockItem) {
        BlockState state = northFacingItemState(blockItem.getBlock().defaultBlockState());
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            state = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        }
        return state;
    }

    private static AABB encompass(AABB a, AABB b) {
        return new AABB(
                Math.min(a.minX, b.minX),
                Math.min(a.minY, b.minY),
                Math.min(a.minZ, b.minZ),
                Math.max(a.maxX, b.maxX),
                Math.max(a.maxY, b.maxY),
                Math.max(a.maxZ, b.maxZ));
    }

    /** Block occupancy: non-empty VoxelShape.bounds(), else unit cube (same class as grass). */
    private static AABB occupancyBounds(BlockState state) {
        BlockGetter getter = EmptyBlockGetter.INSTANCE;
        VoxelShape shape = state.getShape(getter, BlockPos.ZERO, CollisionContext.empty());
        if (shape.isEmpty()) {
            return UNIT;
        }
        return shape.bounds();
    }

    /** Per-axis max extents so scale never underestimates content size. */
    private static AABB maxExtentBounds(AABB a, AABB b) {
        return new AABB(
                0.0,
                0.0,
                0.0,
                Math.max(a.getXsize(), b.getXsize()),
                Math.max(a.getYsize(), b.getYsize()),
                Math.max(a.getZsize(), b.getZsize()));
    }

    /** Force item Geo facing north so bounds match the forced model facing. */
    private static BlockState northFacingItemState(BlockState state) {
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        }
        return state;
    }

    private static void drawFixedItem(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            float fitW,
            float fitH,
            float fitD) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        AABB afterFixed = CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED);
        float scale = CabinetModelBounds.scaleToFit3D(afterFixed, fitW, fitH, fitD);
        CabinetModelBounds.translateToFloorCentered(poseStack, afterFixed, scale);
        itemRenderer.render(
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                bufferSource,
                light,
                OverlayTexture.NO_OVERLAY,
                model);
    }

    private static void renderFixed(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        itemRenderer.render(
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                bufferSource,
                light,
                OverlayTexture.NO_OVERLAY,
                model);
    }
}