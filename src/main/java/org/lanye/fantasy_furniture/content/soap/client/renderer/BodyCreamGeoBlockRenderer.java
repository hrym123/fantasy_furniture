package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyCreamSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code body_cream} geo；2 瓶及以上按层 Pass 绘制 {@code block2}…{@code block6}。 */
@OnlyIn(Dist.CLIENT)
public final class BodyCreamGeoBlockRenderer implements BlockEntityRenderer<BodyCreamBlockEntity> {

    private final GeoBlockRenderer<BodyCreamBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new BodyCreamSingleGeoModel());
    private final BodyCreamStackLayerRenderer layerRenderer = new BodyCreamStackLayerRenderer();

    @Override
    public void render(
            BodyCreamBlockEntity blockEntity,
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
            BodyCreamStackRenderState.set("block" + (i + 2), blockEntity.materialAtLayer(i));
            try {
                layerRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                BodyCreamStackRenderState.clear();
            }
        }
    }
}
