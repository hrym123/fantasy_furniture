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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetYaw;
import org.lanye.reverie_core.geolib.GeolibBlockItem;

/**
 * Cabinet displayed items: shelf floor origin then offset then scale.
 *
 * <p>MODEL blocks scale from occupancy (VoxelShape), floor-aligned with model AABB.
 * Only {@link GeolibBlockItem} uses GEO_FIXED; other ENTITYBLOCK_ANIMATED (chests etc.)
 * scale from occupancy and floor-align with FIXED afterDisplay.
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

    private CabinetDisplayedItemRenderer() {}

    static void draw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            CabinetKind kind,
            int slot,
            int itemYawSteps) {
        CabinetKind.CavityFit cavity = kind.cavityFit(slot);
        float fitW = cavity.width();
        float fitH = cavity.height();
        float fitD = cavity.depth();

        poseStack.pushPose();
        poseStack.translate(kind.itemX(slot), kind.itemFloorY(slot), kind.itemZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(CabinetYaw.degrees(itemYawSteps)));

        if (CabinetDisplayEntities.tryDraw(poseStack, bufferSource, light, stack, level, fitW, fitH, fitD)) {
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
        AABB occupancy = occupancyBounds(state);
        float scale = CabinetModelBounds.scaleToFit3D(occupancy, fitW, fitH, fitD);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        AABB afterFixed = CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED);
        CabinetModelBounds.translateToFloorCentered(poseStack, afterFixed, scale);
        renderFixed(poseStack, bufferSource, light, stack, level);
        return true;
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
