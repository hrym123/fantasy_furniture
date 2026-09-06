package org.lanye.fantasy_furniture.content.furniture.decor.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.client.DrinkwareItemRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.item.DrinkwareBlockItem;
import software.bernie.geckolib.model.GeoModel;

/** 物品栏 / 手持：单件 geo + 材质档贴图。 */
public final class DrinkwareItemGeoModel extends GeoModel<DrinkwareBlockItem> {

    private DrinkwareAppearance appearance() {
        DrinkwareAppearance a = DrinkwareItemRenderer.currentAppearance();
        return a != null ? a : DrinkwareAppearance.defaults();
    }

    @Override
    public ResourceLocation getModelResource(DrinkwareBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/drinkware.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DrinkwareBlockItem animatable) {
        return DrinkwareMaterials.textureLocation(appearance().materialId());
    }

    @Override
    public ResourceLocation getAnimationResource(DrinkwareBlockItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");
    }
}
