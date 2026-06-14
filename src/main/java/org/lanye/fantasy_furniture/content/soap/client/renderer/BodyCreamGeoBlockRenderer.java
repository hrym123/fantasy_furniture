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
import org.lanye.reverie_core.util.ReveriePerfRender;
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
        renderLayers(
                blockEntity, layers, count, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderLayers(
            BodyCreamBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            int count,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (count <= 1) {
            if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.BODY_CREAM)) {
                ReveriePerfRender.geoBlock(
                        "body_cream_mixed_stack",
                        () -> mixedRenderer.render(
                                blockEntity,
                                layers,
                                partialTick,
                                poseStack,
                                bufferSource,
                                packedLight,
                                packedOverlay));
                return;
            }
            ReveriePerfRender.geoBlock(
                    "body_cream",
                    () -> singleRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
            return;
        }
        if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.BODY_CREAM)) {
            ReveriePerfRender.geoBlock(
                    "body_cream_mixed_stack",
                    () -> mixedRenderer.render(
                            blockEntity,
                            layers,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay));
            return;
        }
        if (SoapBottleStackRules.usesCreamFiveSlotStack(layers)) {
            ReveriePerfRender.geoBlock(
                    "body_cream_five_stack",
                    () -> mixedRenderer.renderHomogeneousCreamStack(
                            blockEntity,
                            layers,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay));
            return;
        }
        for (int i = 0; i < count; i++) {
            String bone = BodyCreamAssets.stackBoneForLayerIndex(i);
            if (bone == null) {
                continue;
            }
            BodyCreamStackRenderState.set(bone, blockEntity.materialAtLayer(i));
            try {
                ReveriePerfRender.geoBlock(
                        "body_cream_stack",
                        () -> layerRenderer.render(
                                blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
            } finally {
                BodyCreamStackRenderState.clear();
            }
        }
    }
}
