package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderOffsets;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCarrierOverlayGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 瓶罐摞旁叠架/盒 geo（最小可用）。
 *
 * <p>正式模型应对齐 moonstarfish 组合目录；当前用现有架/盒 geo + {@link SoapBottleCarrierRenderOffsets}。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapBottleCarrierOverlayRenderer {

    private final GeoBlockRenderer<SoapBottleBlockEntity> overlayRenderer =
            new GeoBlockRenderer<>(new SoapBottleCarrierOverlayGeoModel());

    public void renderIfPresent(
            SoapBottleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        SoapStackCarrierKind kind = blockEntity.carrier();
        if (kind == null) {
            return;
        }
        boolean intermediate = blockEntity.carrierIntermediate();
        float[] offset = SoapBottleCarrierRenderOffsets.offset(kind, intermediate);
        SoapBottleCarrierRenderState.set(kind, blockEntity.carrierBoxMaterialId(), intermediate);
        poseStack.pushPose();
        poseStack.translate(offset[0], offset[1], offset[2]);
        try {
            ReveriePerfRender.geoBlock(
                    "soap_bottle_carrier_overlay",
                    () -> overlayRenderer.render(
                            blockEntity,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay));
        } finally {
            poseStack.popPose();
            SoapBottleCarrierRenderState.clear();
        }
    }
}
