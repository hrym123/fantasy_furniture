package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.ShampooBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.ShampooStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.ShampooSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code shampoo} geo；2 瓶及以上按层 Pass 绘制 {@code block1}…{@code block4}。 */
@OnlyIn(Dist.CLIENT)
public final class ShampooGeoBlockRenderer implements BlockEntityRenderer<ShampooBlockEntity> {

    private final GeoBlockRenderer<ShampooBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new ShampooSingleGeoModel());
    private final ShampooStackLayerRenderer stackRenderer = new ShampooStackLayerRenderer();

    @Override
    public void render(
            ShampooBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int layers = blockEntity.visibleLayerCount();
        if (layers <= 1) {
            singleRenderer.render(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        for (int i = 0; i < layers; i++) {
            ShampooStackRenderState.set("block" + (i + 1), blockEntity.materialAtLayer(i));
            try {
                stackRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                ShampooStackRenderState.clear();
            }
        }
    }
}
