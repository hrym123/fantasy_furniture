package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackData;
import org.lanye.fantasy_furniture.content.soap.SoapComboLayouts;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleComboCreamRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCarrierInnerSoapGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCarrierStandaloneGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCreamStandaloneGeoModel;
import org.lanye.reverie_core.composite.OffsetGeoPartLayer;
import org.lanye.reverie_core.composite.PartPose;
import org.lanye.reverie_core.composite.PoseAfterFacingGeoBlockRenderer;
import org.lanye.reverie_core.util.ReveriePerfRender;

/**
 * 瓶罐摞架/盒与完成态乳霜：单件 geo + {@link SoapComboLayouts}；含开盖与内皂叠层。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapBottleCarrierOverlayRenderer {

    private final PoseAfterFacingGeoBlockRenderer<SoapBottleBlockEntity> carrierRenderer =
            new PoseAfterFacingGeoBlockRenderer<>(new SoapBottleCarrierStandaloneGeoModel());
    private final PoseAfterFacingGeoBlockRenderer<SoapBottleBlockEntity> innerSoapRenderer =
            new PoseAfterFacingGeoBlockRenderer<>(new SoapBottleCarrierInnerSoapGeoModel());
    private final PoseAfterFacingGeoBlockRenderer<SoapBottleBlockEntity> creamRenderer =
            new PoseAfterFacingGeoBlockRenderer<>(new SoapBottleCreamStandaloneGeoModel());

    public void renderIfPresent(
            SoapBottleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        SoapBottleStackData data = blockEntity.stackData();
        SoapStackCarrierKind kind = data.carrier();
        if (kind == null) {
            return;
        }
        boolean intermediate = data.carrierIntermediate();
        if (!intermediate) {
            renderCreamIfPresent(
                    blockEntity, data, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        PartPose pose = SoapComboLayouts.carrierPose(kind, intermediate);
        SoapBarAppearance soap = data.carrierHasSoap() ? data.carrierSoap() : null;
        SoapBottleCarrierRenderState.set(
                kind, data.carrierBoxMaterialId(), intermediate, data.carrierBoxOpen(), soap);
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
            if (soap != null) {
                ReveriePerfRender.geoBlock(
                        "soap_bottle_carrier_inner_soap",
                        () -> OffsetGeoPartLayer.renderWithPose(
                                pose,
                                innerSoapRenderer,
                                blockEntity,
                                partialTick,
                                poseStack,
                                bufferSource,
                                packedLight,
                                packedOverlay));
            }
        } finally {
            SoapBottleCarrierRenderState.clear();
        }
    }

    private void renderCreamIfPresent(
            SoapBottleBlockEntity blockEntity,
            SoapBottleStackData data,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        SoapBottleLayer cream = data.slotAt(2);
        if (cream == null || cream.kind() != SoapBottleKind.BODY_CREAM) {
            return;
        }
        SoapBottleComboCreamRenderState.set(cream.materialId());
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
