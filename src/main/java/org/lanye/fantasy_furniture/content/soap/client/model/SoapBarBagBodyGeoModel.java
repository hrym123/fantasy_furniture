package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 套袋态：袋体 geo × 袋色贴图。 */
public final class SoapBarBagBodyGeoModel extends GeoModel<SoapBarBlockEntity> {

    private SoapBarAppearance appearance(SoapBarBlockEntity entity) {
        return SoapBarAppearance.fromState(entity.getBlockState());
    }

    @Override
    public ResourceLocation getModelResource(SoapBarBlockEntity animatable) {
        return appearance(animatable).bagModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBarBlockEntity animatable) {
        return appearance(animatable).bagTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBarBlockEntity animatable) {
        return appearance(animatable).animationLocation();
    }
}
