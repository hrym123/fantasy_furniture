package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackBodyGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapRackInnerSoapGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 架体 Geo + 可选架上 {@code soap_rack_inner_soap} 叠层。 */
@OnlyIn(Dist.CLIENT)
public final class SoapRackGeoBlockRenderer implements BlockEntityRenderer<SoapRackBlockEntity> {

    private final GeoBlockRenderer<SoapRackBlockEntity> bodyRenderer =
            new GeoBlockRenderer<>(new SoapRackBodyGeoModel());
    private final GeoBlockRenderer<SoapRackBlockEntity> innerSoapRenderer =
            new GeoBlockRenderer<>(new SoapRackInnerSoapGeoModel());

    @Override
    public void render(
            SoapRackBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        ReveriePerfRender.geoBlock(
                "soap_rack",
                () -> this.bodyRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        if (blockEntity.getBlockState().getValue(SoapRackBlock.HAS_SOAP)) {
            ReveriePerfRender.geoBlock(
                    "soap_rack_inner_soap",
                    () -> this.innerSoapRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        }
    }
}
