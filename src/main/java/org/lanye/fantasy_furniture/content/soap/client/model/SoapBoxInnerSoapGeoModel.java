package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 盒内皂叠层：复用 {@code soap_bar} 磨损 geo 与颜料贴图。 */
public final class SoapBoxInnerSoapGeoModel extends GeoModel<SoapBoxBlockEntity> {

    private SoapBarAppearance soap(SoapBoxBlockEntity animatable) {
        return animatable.containedSoap();
    }

    @Override
    public ResourceLocation getModelResource(SoapBoxBlockEntity animatable) {
        return soap(animatable).modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBoxBlockEntity animatable) {
        return soap(animatable).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBoxBlockEntity animatable) {
        return soap(animatable).animationLocation();
    }
}
