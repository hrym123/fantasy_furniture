package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyWashBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyWashSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code body_wash} geo；2 瓶及以上按层 Pass 绘制 {@code body_wash1}…{@code body_wash4}。 */
@OnlyIn(Dist.CLIENT)
public final class BodyWashGeoBlockRenderer implements BlockEntityRenderer<BodyWashBlockEntity> {

    private final GeoBlockRenderer<BodyWashBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new BodyWashSingleGeoModel());
    private final BodyWashStackLayerRenderer layerRenderer = new BodyWashStackLayerRenderer();

    @Override
    public void render(
            BodyWashBlockEntity blockEntity,
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
            BodyWashStackRenderState.set("body_wash" + (i + 1), blockEntity.materialAtLayer(i));
            try {
                layerRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                BodyWashStackRenderState.clear();
            }
        }
    }
}
