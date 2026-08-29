package org.lanye.fantasy_furniture.content.furniture.decor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.client.model.ComputerItemGeoModel;
import org.lanye.fantasy_furniture.content.furniture.decor.item.ComputerBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class ComputerItemRenderer extends GeoItemRenderer<ComputerBlockItem> {

    private static final ThreadLocal<ComputerAppearance> RENDER_APPEARANCE = new ThreadLocal<>();
    private static final ThreadLocal<String> RENDER_ASSET_ID = new ThreadLocal<>();

    public ComputerItemRenderer() {
        super(new ComputerItemGeoModel());
    }

    public static ComputerAppearance currentAppearance() {
        return RENDER_APPEARANCE.get();
    }

    public static String currentClosedAssetId() {
        return RENDER_ASSET_ID.get();
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        RENDER_APPEARANCE.set(ComputerAppearance.fromStack(stack));
        if (stack.getItem() instanceof ComputerBlockItem computerItem) {
            RENDER_ASSET_ID.set(computerItem.closedAssetId());
        } else {
            RENDER_ASSET_ID.set("computer_1");
        }
        try {
            super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            RENDER_APPEARANCE.remove();
            RENDER_ASSET_ID.remove();
        }
    }
}
