package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.blockentity.ShampooBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.ShampooSingleGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 单瓶用 {@code shampoo} geo；纯洗发露多瓶合并 Pass；混合摞按 (种类,材质) 分桶绘制。
 *
 * <p>有载体时另叠架/盒 overlay。TODO：完成态第 3 位乳霜应对齐组合目录 {@code 乳霜.bbmodel}。
 */
@OnlyIn(Dist.CLIENT)
public final class ShampooGeoBlockRenderer implements BlockEntityRenderer<ShampooBlockEntity> {

    private final GeoBlockRenderer<ShampooBlockEntity> singleRenderer =
            new GeoBlockRenderer<>(new ShampooSingleGeoModel());
    private final SoapBottleMixedStackRenderer mixedRenderer = new SoapBottleMixedStackRenderer();
    private final SoapBottleCarrierOverlayRenderer carrierOverlay = new SoapBottleCarrierOverlayRenderer();

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
        renderLayers(
                blockEntity, layers, count, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        carrierOverlay.renderIfPresent(
                blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderLayers(
            ShampooBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            int count,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (count <= 1) {
            if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.SHAMPOO)) {
                ReveriePerfRender.geoBlock(
                        "shampoo_mixed_stack",
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
                    "shampoo",
                    () -> singleRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
            return;
        }
        if (SoapBottleMixedStackRenderer.needsMixedPath(layers, SoapBottleKind.SHAMPOO)) {
            ReveriePerfRender.geoBlock(
                    "shampoo_mixed_stack",
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
                "shampoo_stack",
                () -> mixedRenderer.renderHomogeneousKindStack(
                        blockEntity,
                        layers,
                        SoapBottleKind.SHAMPOO,
                        partialTick,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay));
    }
}
