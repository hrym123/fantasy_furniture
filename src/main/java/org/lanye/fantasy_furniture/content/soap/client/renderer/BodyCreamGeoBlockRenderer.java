package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyCreamSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code body_cream} geo；2 瓶及以上按层 Pass 绘制 {@link BodyCreamAssets#STACK_LAYER_BONES}。 */
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
            String bone = BodyCreamAssets.stackBoneForLayerIndex(i);
            if (bone == null) {
                continue;
            }
            BodyCreamStackRenderState.set(bone, blockEntity.materialAtLayer(i));
            try {
                layerRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                BodyCreamStackRenderState.clear();
            }
        }
    }
}
