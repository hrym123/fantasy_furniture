package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBagStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapPaperBagSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单只空袋用 {@code 包装袋_肥皂} geo；多层摞用 {@code blockN} 叠层 geo。 */
@OnlyIn(Dist.CLIENT)
public final class SoapPaperBagGeoBlockRenderer implements BlockEntityRenderer<SoapPaperBagBlockEntity> {

    private final GeoBlockRenderer<SoapPaperBagBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new SoapPaperBagSingleGeoModel());
    private final SoapPaperBagStackLayerRenderer layerRenderer = new SoapPaperBagStackLayerRenderer();

    @Override
    public void render(
            SoapPaperBagBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int layers = blockEntity.layerCount();
        if (layers <= 1) {
            singleRenderer.render(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        for (int i = 0; i < layers; i++) {
            SoapPaperBagStackRenderState.set("block" + (i + 1), blockEntity.materialAtLayer(i));
            try {
                layerRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                SoapPaperBagStackRenderState.clear();
            }
        }
    }
}
