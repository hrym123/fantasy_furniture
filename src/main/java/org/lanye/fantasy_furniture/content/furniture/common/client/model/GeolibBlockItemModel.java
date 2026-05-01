package org.lanye.fantasy_furniture.content.furniture.common.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.core.geolib.GeolibBlockItem;
import software.bernie.geckolib.model.GeoModel;

/**
 * 所有 {@link GeolibBlockItem} 共用的 Geo 模型：资源来自物品上的 {@link org.lanye.fantasy_furniture.core.geolib.GeolibItemAssets}。
 */
public class GeolibBlockItemModel extends GeoModel<GeolibBlockItem> {

    @Override
    public ResourceLocation getModelResource(GeolibBlockItem object) {
        return object.assets().model();
    }

    @Override
    public ResourceLocation getTextureResource(GeolibBlockItem object) {
        return object.assets().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(GeolibBlockItem animatable) {
        return animatable.assets().animation();
    }
}
