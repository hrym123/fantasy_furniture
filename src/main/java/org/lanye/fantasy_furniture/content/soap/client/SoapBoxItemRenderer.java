package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapBoxItemRenderer extends GeoItemRenderer<SoapBoxBlockItem> {

    private static final ThreadLocal<SoapBoxAppearance> RENDER_APPEARANCE = new ThreadLocal<>();

    public SoapBoxItemRenderer() {
        super(new SoapBoxBlockItemModel());
    }

    static SoapBoxAppearance currentAppearance() {
        return RENDER_APPEARANCE.get();
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        RENDER_APPEARANCE.set(SoapBoxAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            RENDER_APPEARANCE.remove();
        }
    }
}
