package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyCreamSingleGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code body_cream} geo；纯乳霜多瓶用 {@code 乳霜_堆叠_x5}；混合摞按层种类分 Pass 绘制。 */
@OnlyIn(Dist.CLIENT)
public final class BodyCreamGeoBlockRenderer implements BlockEntityRenderer<BodyCreamBlockEntity> {

    private final GeoBlockRenderer<BodyCreamBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new BodyCreamSingleGeoModel());
    private final BodyCreamStackLayerRenderer layerRenderer = new BodyCreamStackLayerRenderer();
    private final SoapBottleMixedStackRenderer mixedRenderer = new SoapBottleMixedStackRenderer();

    @Override
    public void render(
            BodyCreamBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        List<SoapBottleLayer> layers = blockEntity.layersView();
        int count = layers.size();
        if (count <= 1) {
            if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.BODY_CREAM)) {
                mixedRenderer.render(
                        blockEntity, layers, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                return;
            }
            singleRenderer.render(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.BODY_CREAM)) {
            mixedRenderer.render(
                    blockEntity, layers, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        if (SoapBottleStackRules.usesCreamFiveSlotStack(layers)) {
            mixedRenderer.renderHomogeneousCreamStack(
                    blockEntity, layers, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        for (int i = 0; i < count; i++) {
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
