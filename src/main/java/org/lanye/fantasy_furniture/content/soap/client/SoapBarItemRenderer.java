package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapBarItemRenderer extends GeoItemRenderer<SoapBarBlockItem> {

    private static final ThreadLocal<SoapBarAppearance> RENDER_APPEARANCE = new ThreadLocal<>();

    public SoapBarItemRenderer() {
        super(new SoapBarBlockItemModel());
    }

    static SoapBarAppearance currentAppearance() {
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
        RENDER_APPEARANCE.set(SoapBarAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            RENDER_APPEARANCE.remove();
        }
    }
}
