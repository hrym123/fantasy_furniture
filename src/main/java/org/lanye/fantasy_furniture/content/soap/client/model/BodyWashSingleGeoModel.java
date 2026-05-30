package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyWashBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 单瓶沐浴露 geo（源自 {@code 沐浴露_默认.bbmodel}）。 */
public final class BodyWashSingleGeoModel extends GeoModel<BodyWashBlockEntity> {

    @Override
    public ResourceLocation getModelResource(BodyWashBlockEntity animatable) {
        return BodyWashAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(BodyWashBlockEntity animatable) {
        return new BodyWashAppearance(animatable.materialAtLayer(0)).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(BodyWashBlockEntity animatable) {
        return BodyWashAssets.singleAnimationLocation();
    }
}
