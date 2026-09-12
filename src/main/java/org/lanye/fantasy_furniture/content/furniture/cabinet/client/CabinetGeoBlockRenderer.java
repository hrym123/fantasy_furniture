package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * 柜体与展品共用同一套 {@code translate(0.5)+rotateBlock}，避免自写朝向与 Gecko 不一致。
 * 柜子1型按 {@link CabinetSegment} 切换 cell/2x/2z/2s geo。
 */
@OnlyIn(Dist.CLIENT)
public final class CabinetGeoBlockRenderer extends ReverieGeoBlockRenderer<CabinetBlockEntity> {

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
                        // 拼装变体暂共用 cabinet_1.png
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "textures/block/" + animatable.kind().assetId() + ".png");
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
                        var state = animatable.getBlockState();
                        if (state.hasProperty(CabinetBlock.SEGMENT)) {
                            segment = state.getValue(CabinetBlock.SEGMENT);
                        }
                        return segment.cabinet1GeoStem();
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
            restoreBoneVisibility(oldHidden);
        }
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
