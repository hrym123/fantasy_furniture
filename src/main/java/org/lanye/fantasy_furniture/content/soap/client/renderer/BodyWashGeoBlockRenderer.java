package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyWashBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyWashSingleGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶（仅槽 0）用默认 geo；多瓶 / 空槽 / 载体用按槽骨骼 Pass。 */
@OnlyIn(Dist.CLIENT)
public final class BodyWashGeoBlockRenderer implements BlockEntityRenderer<BodyWashBlockEntity> {

    private final GeoBlockRenderer<BodyWashBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new BodyWashSingleGeoModel());
    private final SoapBottleMixedStackRenderer mixedRenderer = new SoapBottleMixedStackRenderer();
    private final SoapBottleCarrierOverlayRenderer carrierOverlay = new SoapBottleCarrierOverlayRenderer();

    @Override
    public void render(
            BodyWashBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int count = blockEntity.layerCount();
        int first = blockEntity.stackData().firstOccupiedSlot();
        if (count == 1 && first == 0 && !blockEntity.hasCarrier()) {
            ReveriePerfRender.geoBlock(
                    "body_wash",
                    () -> singleRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        } else if (count > 0) {
            ReveriePerfRender.geoBlock(
                    "body_wash_slots",
                    () -> mixedRenderer.renderFromSlots(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        }
        carrierOverlay.renderIfPresent(
                blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
