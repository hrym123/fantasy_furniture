package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.client.model.ShampooBlockItemModel;
import org.lanye.fantasy_furniture.content.soap.item.ShampooBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class ShampooItemRenderer extends GeoItemRenderer<ShampooBlockItem> {

    public ShampooItemRenderer() {
        super(new ShampooBlockItemModel());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        ShampooItemRenderState.set(ShampooAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            ShampooItemRenderState.clear();
        }
    }
}
