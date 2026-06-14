package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBarBagBodyGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBarBodyGeoModel;
import org.lanye.reverie_core.util.ReveriePerfRender;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 地上肥皂：未套袋仅皂体；套袋后袋体 + 皂体双 Pass。 */
@OnlyIn(Dist.CLIENT)
public final class SoapBarGeoBlockRenderer implements BlockEntityRenderer<SoapBarBlockEntity> {

    private final GeoBlockRenderer<SoapBarBlockEntity> bodyRenderer =
            new GeoBlockRenderer<>(new SoapBarBodyGeoModel());
    private final GeoBlockRenderer<SoapBarBlockEntity> bagRenderer =
            new GeoBlockRenderer<>(new SoapBarBagBodyGeoModel());

    @Override
    public void render(
            SoapBarBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        SoapBarAppearance appearance = SoapBarAppearance.fromState(blockEntity.getBlockState());
        if (appearance.isPackaged()) {
            ReveriePerfRender.geoBlock(
                    "soap_bar_bag",
                    () -> bagRenderer.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        }
        ReveriePerfRender.geoBlock(
                "soap_bar",
                () -> bodyRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
    }
}
