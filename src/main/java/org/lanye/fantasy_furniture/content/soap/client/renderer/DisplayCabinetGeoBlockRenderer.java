package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetBottleKind;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetStoredBottle;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.DisplayCabinetBottleRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.DisplayCabinetBodyWashOverlayModel;
import org.lanye.fantasy_furniture.content.soap.client.model.DisplayCabinetGeoModel;
import org.lanye.fantasy_furniture.content.soap.client.model.DisplayCabinetShampooOverlayModel;
import org.lanye.reverie_core.util.ReveriePerfRender;

@OnlyIn(Dist.CLIENT)
public final class DisplayCabinetGeoBlockRenderer implements BlockEntityRenderer<DisplayCabinetBlockEntity> {

    private final DisplayCabinetLayerRenderer cabinetRenderer =
            new DisplayCabinetLayerRenderer(new DisplayCabinetGeoModel());
    private final DisplayCabinetBottleOverlayRenderer washOverlayRenderer =
            new DisplayCabinetBottleOverlayRenderer(new DisplayCabinetBodyWashOverlayModel());
    private final DisplayCabinetBottleOverlayRenderer shampooOverlayRenderer =
            new DisplayCabinetBottleOverlayRenderer(new DisplayCabinetShampooOverlayModel());

    @Override
    public void render(
            DisplayCabinetBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        ReveriePerfRender.geoBlock(
                "display_cabinet",
                () -> cabinetRenderer.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay));
        ReveriePerfRender.geoBlock(
                "display_cabinet_bottle_overlay",
                () -> renderStoredBottleOverlays(
                        blockEntity,
                        washOverlayRenderer,
                        shampooOverlayRenderer,
                        partialTick,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay));
    }

    private static void renderStoredBottleOverlays(
            DisplayCabinetBlockEntity blockEntity,
            DisplayCabinetBottleOverlayRenderer washRenderer,
            DisplayCabinetBottleOverlayRenderer shampooRenderer,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int slotIndex = 0;
        for (DisplayCabinetStoredBottle bottle : blockEntity.bottlesView()) {
            DisplayCabinetBottleRenderState.setBottleOnly(
                    DisplayCabinetAssets.slotBottleBones(bottle.kind(), slotIndex),
                    bottle.kind(),
                    bottle.materialId());
            try {
                DisplayCabinetBottleOverlayRenderer renderer =
                        bottle.kind() == DisplayCabinetBottleKind.BODY_WASH
                                ? washRenderer
                                : shampooRenderer;
                renderer.render(
                        blockEntity,
                        partialTick,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay);
            } finally {
                DisplayCabinetBottleRenderState.clear();
            }
            slotIndex++;
        }
    }
}
