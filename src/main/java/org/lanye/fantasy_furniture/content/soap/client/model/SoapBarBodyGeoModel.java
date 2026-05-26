package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 地上肥皂：磨损 geo × 颜料贴图。 */
public final class SoapBarBodyGeoModel extends GeoModel<SoapBarBlockEntity> {

    private SoapBarAppearance appearance(SoapBarBlockEntity entity) {
        return SoapBarAppearance.fromState(entity.getBlockState());
    }

    @Override
    public ResourceLocation getModelResource(SoapBarBlockEntity animatable) {
        return appearance(animatable).modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBarBlockEntity animatable) {
        return appearance(animatable).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBarBlockEntity animatable) {
        return appearance(animatable).animationLocation();
    }
}
