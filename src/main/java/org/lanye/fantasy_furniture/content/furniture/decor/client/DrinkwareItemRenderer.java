package org.lanye.fantasy_furniture.content.furniture.decor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.client.model.DrinkwareItemGeoModel;
import org.lanye.fantasy_furniture.content.furniture.decor.item.DrinkwareBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class DrinkwareItemRenderer extends GeoItemRenderer<DrinkwareBlockItem> {

    private static final ThreadLocal<DrinkwareAppearance> RENDER_APPEARANCE = new ThreadLocal<>();

    public DrinkwareItemRenderer() {
        super(new DrinkwareItemGeoModel());
    }

    public static DrinkwareAppearance currentAppearance() {
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
        RENDER_APPEARANCE.set(DrinkwareAppearance.fromStack(stack));
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            RENDER_APPEARANCE.remove();
        }
    }
}
