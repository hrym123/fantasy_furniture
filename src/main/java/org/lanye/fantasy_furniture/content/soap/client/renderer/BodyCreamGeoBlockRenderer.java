package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyCreamSingleGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code body_cream} geo；纯乳霜多瓶用 {@code 乳霜_堆叠_x5}；混合摞按 (种类,材质) 分桶绘制。
 *
 * <p>有载体时另叠单件架/盒 + 布局偏移；完成态第 3 位乳霜同。
 */
@OnlyIn(Dist.CLIENT)
public final class BodyCreamGeoBlockRenderer implements BlockEntityRenderer<BodyCreamBlockEntity> {

    private final GeoBlockRenderer<BodyCreamBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new BodyCreamSingleGeoModel());
    private final SoapBottleMixedStackRenderer mixedRenderer = new SoapBottleMixedStackRenderer();
    private final SoapBottleCarrierOverlayRenderer carrierOverlay = new SoapBottleCarrierOverlayRenderer();

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
        carrierOverlay.renderIfPresent(
                blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
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
        ReveriePerfRender.geoBlock(
                "body_cream_stack",
                () -> mixedRenderer.renderHomogeneousKindStack(
                        blockEntity,
                        layers,
                        SoapBottleKind.BODY_CREAM,
                        partialTick,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay));
    }
}
