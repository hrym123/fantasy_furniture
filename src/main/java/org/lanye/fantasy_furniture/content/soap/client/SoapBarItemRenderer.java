package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 物品栏用 2D 物品材质；手持 / 掉落等仍用 Geo。 */
@OnlyIn(Dist.CLIENT)
public final class SoapBarItemRenderer extends GeoItemRenderer<SoapBarBlockItem> {

    private static final ThreadLocal<SoapBarAppearance> RENDER_APPEARANCE = new ThreadLocal<>();

    private final GeoItemRenderer<SoapBarBlockItem> bagRenderer =
            new GeoItemRenderer<>(new GeoModel<>() {
                private SoapBarAppearance appearance() {
                    SoapBarAppearance a = RENDER_APPEARANCE.get();
                    return a != null ? a : SoapBarAppearance.defaults();
                }

                @Override
                public ResourceLocation getModelResource(SoapBarBlockItem animatable) {
                    return appearance().bagModelLocation();
                }

                @Override
                public ResourceLocation getTextureResource(SoapBarBlockItem animatable) {
                    return appearance().bagTextureLocation();
                }

                @Override
                public ResourceLocation getAnimationResource(SoapBarBlockItem animatable) {
                    return appearance().animationLocation();
                }
            });

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
        if (displayContext == ItemDisplayContext.GUI) {
            return;
        }
        SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
        RENDER_APPEARANCE.set(appearance);
        try {
            if (appearance.isPackaged()) {
                bagRenderer.renderByItem(
                        stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
            }
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            RENDER_APPEARANCE.remove();
        }
    }
}
