package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapPaperBagBlockItemModel;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBagBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapPaperBagItemRenderer extends GeoItemRenderer<SoapPaperBagBlockItem> {

    public SoapPaperBagItemRenderer() {
        super(new SoapPaperBagBlockItemModel());
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
        SoapPaperBagItemRenderState.set(SoapPaperBagAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            SoapPaperBagItemRenderState.clear();
        }
    }
}
