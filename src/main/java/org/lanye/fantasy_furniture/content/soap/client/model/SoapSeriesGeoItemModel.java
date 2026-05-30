package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.item.SoapSeriesBlockItem;
import software.bernie.geckolib.model.GeoModel;

public final class SoapSeriesGeoItemModel extends GeoModel<SoapSeriesBlockItem> {

    @Override
    public ResourceLocation getModelResource(SoapSeriesBlockItem object) {
        return object.assets().model();
    }

    @Override
    public ResourceLocation getTextureResource(SoapSeriesBlockItem object) {
        return object.assets().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapSeriesBlockItem animatable) {
        return animatable.assets().animation();
    }
}
