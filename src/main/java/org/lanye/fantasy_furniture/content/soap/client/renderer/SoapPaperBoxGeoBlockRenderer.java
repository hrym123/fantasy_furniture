package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBoxStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapPaperBoxSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单盒用 {@code soap_paper_box} geo；2 盒及以上按层 Pass 绘制 {@code block1}…{@code block7}。 */
@OnlyIn(Dist.CLIENT)
public final class SoapPaperBoxGeoBlockRenderer implements BlockEntityRenderer<SoapPaperBoxBlockEntity> {

    private final GeoBlockRenderer<SoapPaperBoxBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new SoapPaperBoxSingleGeoModel());
    private final SoapPaperBoxStackLayerRenderer layerRenderer = new SoapPaperBoxStackLayerRenderer();

    @Override
    public void render(
            SoapPaperBoxBlockEntity blockEntity,
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
        BlockState state = blockEntity.getBlockState();
        int stackStyle =
                state.getBlock() instanceof SoapPaperBoxBlock block
                        ? state.getValue(block.STACK_STYLE)
                        : 1;
        for (int i = 0; i < layers; i++) {
            SoapPaperBoxStackRenderState.set("block" + (i + 1), blockEntity.materialAtLayer(i), stackStyle);
            try {
                layerRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                SoapPaperBoxStackRenderState.clear();
            }
        }
    }
}
