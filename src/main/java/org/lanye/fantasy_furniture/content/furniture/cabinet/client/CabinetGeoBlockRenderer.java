package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetJointShelfPick;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetMaterials;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.GeoModel;

/**
 * 柜体与展品共用同一套 {@code translate(0.5)+rotateBlock}，避免自写朝向与 Gecko 不一致。
 * 柜子1型按 {@link CabinetSegment} 与 {@link CabinetBlock#SIDE_OPEN_NEG}/{@link CabinetBlock#SIDE_OPEN_POS}
 * 切换 cell/2x/2z/2s 或 open / open_px / open_both；拆除隔板按立方体 Y 带隐藏（无 shelf_* 骨）。
 */
@OnlyIn(Dist.CLIENT)
public final class CabinetGeoBlockRenderer extends ReverieGeoBlockRenderer<CabinetBlockEntity> {

    @Nullable
    private CabinetBlockEntity shelfHideAnimatable;

    public CabinetGeoBlockRenderer() {
        super(
                new GeoModel<CabinetBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(CabinetBlockEntity animatable) {
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID, "geo/block/" + geoStem(animatable) + ".geo.json");
                    }

                    @Override
                    public ResourceLocation getTextureResource(CabinetBlockEntity animatable) {
                        int material = CabinetMaterials.DEFAULT;
                        var state = animatable.getBlockState();
                        if (state.hasProperty(CabinetBlock.MATERIAL)) {
                            material = state.getValue(CabinetBlock.MATERIAL);
                        }
                        return CabinetMaterials.textureLocation(animatable.kind(), material);
                    }

                    @Override
                    public ResourceLocation getAnimationResource(CabinetBlockEntity animatable) {
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "animations/block/" + geoStem(animatable) + ".animation.json");
                    }

                    private static String geoStem(CabinetBlockEntity animatable) {
                        if (animatable.kind() != CabinetKind.CABINET_1) {
                            return animatable.kind().assetId();
                        }
                        CabinetSegment segment = CabinetSegment.ALONE;
                        boolean openNeg = false;
                        boolean openPos = false;
                        var state = animatable.getBlockState();
                        if (state.hasProperty(CabinetBlock.SEGMENT)) {
                            segment = state.getValue(CabinetBlock.SEGMENT);
                        }
                        if (state.hasProperty(CabinetBlock.SIDE_OPEN_NEG)) {
                            openNeg = state.getValue(CabinetBlock.SIDE_OPEN_NEG);
                        }
                        if (state.hasProperty(CabinetBlock.SIDE_OPEN_POS)) {
                            openPos = state.getValue(CabinetBlock.SIDE_OPEN_POS);
                        }
                        return segment.cabinet1GeoStem(openNeg, openPos);
                    }
                },
                GeoRenderTier.STATIC);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            CabinetBlockEntity animatable,
            BakedGeoModel model,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        Map<GeoBone, Boolean> oldHidden = applyShelfBoneVisibility(model, animatable);
        CabinetBlockEntity prevHide = shelfHideAnimatable;
        shelfHideAnimatable = animatable;
        try {
            if (!isReRender) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0, 0.5);
                rotateBlock(getFacing(animatable), poseStack);
                super.actuallyRender(
                        poseStack,
                        animatable,
                        model,
                        renderType,
                        bufferSource,
                        buffer,
                        true,
                        partialTick,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha);
                renderDisplayedItems(poseStack, animatable, bufferSource, packedLight);
                poseStack.popPose();
                return;
            }
            super.actuallyRender(
                    poseStack,
                    animatable,
                    model,
                    renderType,
                    bufferSource,
                    buffer,
                    true,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha);
        } finally {
            shelfHideAnimatable = prevHide;
            restoreBoneVisibility(oldHidden);
        }
    }

    @Override
    public void renderCubesOfBone(
            PoseStack poseStack,
            GeoBone bone,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        if (bone.isHidden()) {
            return;
        }
        CabinetBlockEntity be = shelfHideAnimatable;
        for (GeoCube cube : bone.getCubes()) {
            if (be != null && be.kind() == CabinetKind.CABINET_1 && shouldHideCabinet1Cube(cube, be)) {
                continue;
            }
            poseStack.pushPose();
            renderCube(poseStack, cube, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        }
    }

    /**
     * 柜子1 拼装 geo 无 {@code shelf_*} 骨：按立方体厚度（像素）与顶点 Y（方块）对齐
     * {@link CabinetKind#shelfLocalAabb}。注意 GeckoLib 的 {@link GeoCube#size()} 为像素，不是 /16。
     *
     * <p>连接中隔为上下各 1px、分属两格：本格拥有下半（Y15–16），上格底部 1px 属下格中隔的上半片，
     * 拆中隔时须两侧一并隐藏。
     */
    private static boolean shouldHideCabinet1Cube(GeoCube cube, CabinetBlockEntity be) {
        Vec3 sizePx = cube.size();
        // 双侧隔板约 2px；连接中隔上下半片 / 开口底板常为 1px
        if (sizePx.y < 0.9 || sizePx.y > 2.5) {
            return false;
        }
        // 闭口约 12×14；开口约 14×14；both 满宽 16；2S 顶盖略宽 13
        if (sizePx.x < 10.0 || sizePx.z < 12.0) {
            return false;
        }
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (GeoQuad quad : cube.quads()) {
            for (GeoVertex vertex : quad.vertices()) {
                float y = vertex.position().y;
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        if (!(minY < maxY)) {
            return false;
        }
        double centerY = (minY + maxY) * 0.5;
        CabinetSegment segment = be.segment();
        var state = be.getBlockState();
        boolean openNeg =
                state.hasProperty(CabinetBlock.SIDE_OPEN_NEG) && state.getValue(CabinetBlock.SIDE_OPEN_NEG);
        boolean openPos =
                state.hasProperty(CabinetBlock.SIDE_OPEN_POS) && state.getValue(CabinetBlock.SIDE_OPEN_POS);
        for (int shelf = 0; shelf < CabinetKind.SHELF_COUNT; shelf++) {
            if (shelf == 2) {
                continue;
            }
            if (!be.kind().shelfInSegment(shelf, segment) || be.isShelfPresent(shelf)) {
                continue;
            }
            AABB box = be.kind().shelfLocalAabb(shelf, openNeg, openPos);
            // 1px 半片相对名义盒略偏；放宽上下各约 1px
            if (centerY >= box.minY - 0.04 && centerY <= box.maxY + 0.04) {
                return true;
            }
        }
        // 上格（middle/top）底部 1px：归属下格连接中隔
        if (centerY <= 1.5 / 16.0 + 0.04 && isLowerJointShelfAbsent(be)) {
            return true;
        }
        return false;
    }

    /** 同柱下格的连接中隔已拆除（或本格无下格中隔半片）。 */
    private static boolean isLowerJointShelfAbsent(CabinetBlockEntity be) {
        CabinetSegment segment = be.segment();
        if (segment != CabinetSegment.MIDDLE && segment != CabinetSegment.TOP) {
            return false;
        }
        Level level = be.getLevel();
        if (level == null || !be.getBlockState().hasProperty(CabinetBlock.FACING)) {
            return false;
        }
        Direction facing = be.getBlockState().getValue(CabinetBlock.FACING);
        BlockPos below = be.getBlockPos().below();
        if (!CabinetBlock.areLinkedInStack(level, be.getBlockPos(), below, facing)) {
            return false;
        }
        if (!(level.getBlockEntity(below) instanceof CabinetBlockEntity lower)
                || lower.kind() != CabinetKind.CABINET_1) {
            return false;
        }
        return !lower.isShelfActive(CabinetJointShelfPick.JOINT_SHELF);
    }

    private static Map<GeoBone, Boolean> applyShelfBoneVisibility(
            BakedGeoModel model, CabinetBlockEntity animatable) {
        Map<GeoBone, Boolean> oldHidden = new HashMap<>();
        for (GeoBone bone : flattenBones(model)) {
            String name = bone.getName();
            if (name == null || !name.startsWith("shelf_")) {
                continue;
            }
            int idx;
            try {
                idx = Integer.parseInt(name.substring("shelf_".length()));
            } catch (NumberFormatException ex) {
                continue;
            }
            boolean hide = !animatable.isShelfPresent(idx);
            if (!hide) {
                continue;
            }
            oldHidden.put(bone, bone.isHidden());
            bone.setHidden(true);
        }
        return oldHidden;
    }

    private static void restoreBoneVisibility(Map<GeoBone, Boolean> oldHidden) {
        oldHidden.forEach(GeoBone::setHidden);
    }

    private static List<GeoBone> flattenBones(BakedGeoModel model) {
        List<GeoBone> result = new ArrayList<>();
        for (GeoBone top : model.topLevelBones()) {
            collectBones(top, result);
        }
        return result;
    }

    private static void collectBones(GeoBone current, List<GeoBone> out) {
        out.add(current);
        for (GeoBone child : current.getChildBones()) {
            collectBones(child, out);
        }
    }

    private void renderDisplayedItems(
            PoseStack poseStack,
            CabinetBlockEntity animatable,
            MultiBufferSource bufferSource,
            int packedLight) {
        int n = animatable.storageSlotCount();
        for (int slot = 0; slot < n; slot++) {
            ItemStack stack = animatable.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CabinetDisplayedItemRenderer.draw(
                    poseStack,
                    bufferSource,
                    packedLight,
                    stack,
                    animatable.getLevel(),
                    animatable,
                    slot,
                    animatable.itemYaw(slot));
        }
    }
}
