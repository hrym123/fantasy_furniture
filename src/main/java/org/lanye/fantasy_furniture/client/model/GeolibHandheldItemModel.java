package org.lanye.fantasy_furniture.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.geolib.GeolibHandheldItem;
import software.bernie.geckolib.model.GeoModel;

/** 所有 {@link GeolibHandheldItem} 共用的 Geo 模型，资源来自 {@link org.lanye.fantasy_furniture.geolib.GeolibItemAssets}。 */
public class GeolibHandheldItemModel extends GeoModel<GeolibHandheldItem> {

    @Override
    public ResourceLocation getModelResource(GeolibHandheldItem object) {
        return object.assets().model();
    }

    @Override
    public ResourceLocation getTextureResource(GeolibHandheldItem object) {
        return object.assets().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(GeolibHandheldItem animatable) {
        return animatable.assets().animation();
    }
}
