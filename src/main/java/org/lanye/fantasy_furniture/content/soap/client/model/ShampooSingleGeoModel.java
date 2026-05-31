package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.ShampooBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 单瓶洗发露 geo（源自 {@code 洗发露_默认.bbmodel}）。 */
public final class ShampooSingleGeoModel extends GeoModel<ShampooBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ShampooBlockEntity animatable) {
        return ShampooAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(ShampooBlockEntity animatable) {
        return new ShampooAppearance(animatable.materialAtLayer(0)).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(ShampooBlockEntity animatable) {
        return ShampooAssets.singleAnimationLocation();
    }
}
