package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapRackComboScheme;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapRackBottleRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackBodyGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackBottleOverlayGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackComboCream2GeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackComboFullGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackInnerSoapGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 肥皂架渲染：完成方案 1 / 3 切组合模型档；其余为架体 + 可选架上皂 + 瓶罐 overlay（方案 2 / 中间态）。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapRackGeoBlockRenderer implements BlockEntityRenderer<SoapRackBlockEntity> {

    /** 北向局部偏移（方块单位），自底向上排序后的瓶罐陈列位。 */
    private static final float[][] BOTTLE_SLOTS = {
        {0.22f, 0.0f, 0.12f},
        {-0.22f, 0.0f, 0.12f},
        {0.12f, 0.0f, -0.18f},
        {-0.12f, 0.0f, -0.18f},
    };

    private final GeoBlockRenderer<SoapRackBlockEntity> bodyRenderer =
            new GeoBlockRenderer<>(new SoapRackBodyGeoModel());
    private final GeoBlockRenderer<SoapRackBlockEntity> innerSoapRenderer =
            new GeoBlockRenderer<>(new SoapRackInnerSoapGeoModel());
    private final GeoBlockRenderer<SoapRackBlockEntity> comboFullRenderer =
            new GeoBlockRenderer<>(new SoapRackComboFullGeoModel());
    private final GeoBlockRenderer<SoapRackBlockEntity> comboCream2Renderer =
            new GeoBlockRenderer<>(new SoapRackComboCream2GeoModel());
    private final GeoBlockRenderer<SoapRackBlockEntity> bottleOverlayRenderer =
            new GeoBlockRenderer<>(new SoapRackBottleOverlayGeoModel());

    @Override
    public void render(
            SoapRackBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        SoapRackComboScheme scheme = blockEntity.completedComboScheme();
        if (scheme == SoapRackComboScheme.LIQUIDS_CREAM_RACK_SOAP) {
            ReveriePerfRender.geoBlock(
                    "soap_rack_combo_full",
                    () -> comboFullRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
            return;
        }
        if (scheme == SoapRackComboScheme.CREAM2_RACK_SOAP) {
            ReveriePerfRender.geoBlock(
                    "soap_rack_combo_cream2",
                    () -> comboCream2Renderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
            return;
        }

        ReveriePerfRender.geoBlock(
                "soap_rack",
                () -> bodyRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        if (blockEntity.getBlockState().getValue(SoapRackBlock.HAS_SOAP)) {
            ReveriePerfRender.geoBlock(
                    "soap_rack_inner_soap",
                    () -> innerSoapRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        }
        List<SoapBottleLayer> bottles = blockEntity.bottlesView();
        if (!bottles.isEmpty()) {
            ReveriePerfRender.geoBlock(
                    "soap_rack_bottle_overlay",
                    () -> renderBottleOverlays(
                            blockEntity,
                            bottles,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay));
        }
    }

    private void renderBottleOverlays(
            SoapRackBlockEntity blockEntity,
            List<SoapBottleLayer> bottles,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int n = Math.min(bottles.size(), BOTTLE_SLOTS.length);
        for (int i = 0; i < n; i++) {
            SoapBottleLayer layer = bottles.get(i);
            float[] slot = BOTTLE_SLOTS[i];
            SoapRackBottleRenderState.set(layer.kind(), layer.materialId());
            poseStack.pushPose();
            poseStack.translate(slot[0], slot[1], slot[2]);
            try {
                bottleOverlayRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                poseStack.popPose();
                SoapRackBottleRenderState.clear();
            }
        }
    }
}
