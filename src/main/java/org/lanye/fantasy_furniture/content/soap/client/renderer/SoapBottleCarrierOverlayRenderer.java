package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapComboLayouts;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleComboCreamRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCarrierStandaloneGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCreamStandaloneGeoModel;
import org.lanye.reverie_core.composite.OffsetGeoPartLayer;
import org.lanye.reverie_core.composite.PartPose;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 瓶罐摞架/盒与完成态乳霜：单件 geo + {@link SoapComboLayouts} 偏移（不再切组合专用 geo）。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapBottleCarrierOverlayRenderer {

    private final GeoBlockRenderer<SoapBottleBlockEntity> carrierRenderer =
            new GeoBlockRenderer<>(new SoapBottleCarrierStandaloneGeoModel());
    private final GeoBlockRenderer<SoapBottleBlockEntity> creamRenderer =
            new GeoBlockRenderer<>(new SoapBottleCreamStandaloneGeoModel());

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
        if (!intermediate) {
            renderCreamIfPresent(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        PartPose pose = SoapComboLayouts.carrierPose(kind, intermediate);
        SoapBottleCarrierRenderState.set(kind, blockEntity.carrierBoxMaterialId(), intermediate);
        try {
            ReveriePerfRender.geoBlock(
                    "soap_bottle_carrier_offset",
                    () -> OffsetGeoPartLayer.renderWithPose(
                            pose,
                            carrierRenderer,
                            blockEntity,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay));
        } finally {
            SoapBottleCarrierRenderState.clear();
        }
    }

    private void renderCreamIfPresent(
            SoapBottleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        List<SoapBottleLayer> layers = blockEntity.layersView();
        if (layers.size() < 3 || layers.get(2).kind() != SoapBottleKind.BODY_CREAM) {
            return;
        }
        SoapBottleComboCreamRenderState.set(layers.get(2).materialId());
        try {
            ReveriePerfRender.geoBlock(
                    "soap_bottle_cream_offset",
                    () -> OffsetGeoPartLayer.renderWithPose(
                            SoapComboLayouts.CREAM_DONE_FROM_SINGLE,
                            creamRenderer,
                            blockEntity,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay));
        } finally {
            SoapBottleComboCreamRenderState.clear();
        }
    }
}
