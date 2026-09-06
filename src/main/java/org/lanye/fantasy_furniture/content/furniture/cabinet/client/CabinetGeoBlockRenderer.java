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
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

/**
 * 柜体与展品共用同一套 {@code translate(0.5)+rotateBlock}，避免自写朝向与 Gecko 不一致。
 */
@OnlyIn(Dist.CLIENT)
public final class CabinetGeoBlockRenderer extends ReverieGeoBlockRenderer<CabinetBlockEntity> {

    public CabinetGeoBlockRenderer() {
        super(
                new GeoModel<CabinetBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(CabinetBlockEntity animatable) {
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "geo/block/" + animatable.kind().assetId() + ".geo.json");
                    }

                    @Override
                    public ResourceLocation getTextureResource(CabinetBlockEntity animatable) {
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "textures/block/" + animatable.kind().assetId() + ".png");
                    }

                    @Override
                    public ResourceLocation getAnimationResource(CabinetBlockEntity animatable) {
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "animations/block/" + animatable.kind().assetId() + ".animation.json");
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
    }

    private void renderDisplayedItems(
            PoseStack poseStack,
            CabinetBlockEntity animatable,
            MultiBufferSource bufferSource,
            int packedLight) {
        CabinetKind kind = animatable.kind();
        int n = kind.slotCount();
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
                    kind,
                    slot,
                    animatable.itemYaw(slot));
        }
    }
}
