package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleComboCreamRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleCarrierOverlayGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBottleComboCreamGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 瓶罐摞上组合架/盒（及完成态第 3 位组合乳霜）overlay。
 *
 * <p>几何已按组合目录 bbmodel 就位，不再做 PoseStack 临时平移。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapBottleCarrierOverlayRenderer {

    private final GeoBlockRenderer<SoapBottleBlockEntity> carrierRenderer =
            new GeoBlockRenderer<>(new SoapBottleCarrierOverlayGeoModel());
    private final GeoBlockRenderer<SoapBottleBlockEntity> creamRenderer =
            new GeoBlockRenderer<>(new SoapBottleComboCreamGeoModel());

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
            renderComboCreamIfPresent(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        SoapBottleCarrierRenderState.set(kind, blockEntity.carrierBoxMaterialId(), intermediate);
        try {
            ReveriePerfRender.geoBlock(
                    "soap_bottle_carrier_overlay",
                    () -> carrierRenderer.render(
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

    private void renderComboCreamIfPresent(
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
                    "soap_bottle_combo_cream",
                    () -> creamRenderer.render(
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
