package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.model.BedPlate6AccessoryGeoItemModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6GeolibDecorItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 床板 6 床单 / 被套 / 枕头在 GUI 与手中的 GeckoLib 渲染。 */
public final class BedPlate6AccessoryGeoItemRenderer extends GeoItemRenderer<BedPlate6GeolibDecorItem> {

    public BedPlate6AccessoryGeoItemRenderer() {
        super(new BedPlate6AccessoryGeoItemModel());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (displayContext == ItemDisplayContext.GUI && usesFlatItemIcon(stack)) {
            return;
        }
        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /** 大号枕头·纯色：创造栏 / 物品栏用 {@code models/item} 单材质图，手持仍走 Geo。 */
    private static boolean usesFlatItemIcon(ItemStack stack) {
        if (!(stack.getItem() instanceof BedPlate6LargePillowItem pillow)) {
            return false;
        }
        return "plain".equals(BedPlate6LargePillowStyles.resourceSlug(pillow.getStyleId()));
    }
}
