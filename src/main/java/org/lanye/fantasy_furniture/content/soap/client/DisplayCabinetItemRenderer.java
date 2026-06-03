package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAppearance;
import org.lanye.fantasy_furniture.content.soap.client.model.DisplayCabinetBlockItemModel;
import org.lanye.fantasy_furniture.content.soap.item.DisplayCabinetBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class DisplayCabinetItemRenderer extends GeoItemRenderer<DisplayCabinetBlockItem> {

    public DisplayCabinetItemRenderer() {
        super(new DisplayCabinetBlockItemModel());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        DisplayCabinetItemRenderState.set(DisplayCabinetAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            DisplayCabinetItemRenderState.clear();
        }
    }
}
