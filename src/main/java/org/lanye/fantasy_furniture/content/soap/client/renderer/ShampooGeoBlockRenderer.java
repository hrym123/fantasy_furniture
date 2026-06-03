package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackSlots;
import org.lanye.fantasy_furniture.content.soap.blockentity.ShampooBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.ShampooStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.ShampooSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code shampoo} geo；纯洗发露多瓶用 {@code block1}…{@code block4}；混合摞按层种类分 Pass 绘制。 */
@OnlyIn(Dist.CLIENT)
public final class ShampooGeoBlockRenderer implements BlockEntityRenderer<ShampooBlockEntity> {

    private final GeoBlockRenderer<ShampooBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new ShampooSingleGeoModel());
    private final ShampooStackLayerRenderer stackRenderer = new ShampooStackLayerRenderer();
    private final SoapBottleMixedStackRenderer mixedRenderer = new SoapBottleMixedStackRenderer();

    @Override
    public void render(
            ShampooBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        List<SoapBottleLayer> layers = blockEntity.layersView();
        int count = blockEntity.visibleLayerCount();
        if (count <= 1) {
            if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.SHAMPOO)) {
                mixedRenderer.render(
                        blockEntity, layers, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                return;
            }
            singleRenderer.render(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.SHAMPOO)) {
            mixedRenderer.render(
                    blockEntity, layers, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        for (int i = 0; i < count; i++) {
            ShampooStackRenderState.set(
                    ShampooAssets.stackBoneForLayerIndex(i), blockEntity.materialAtLayer(i));
            try {
                stackRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                ShampooStackRenderState.clear();
            }
        }
    }
}
