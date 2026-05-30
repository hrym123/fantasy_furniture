package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapPaperBoxBlockItemModel;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBoxBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapPaperBoxItemRenderer extends GeoItemRenderer<SoapPaperBoxBlockItem> {

    public SoapPaperBoxItemRenderer() {
        super(new SoapPaperBoxBlockItemModel());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (displayContext == ItemDisplayContext.GUI) {
            return;
        }
        SoapPaperBoxItemRenderState.set(SoapPaperBoxAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            SoapPaperBoxItemRenderState.clear();
        }
    }
}
