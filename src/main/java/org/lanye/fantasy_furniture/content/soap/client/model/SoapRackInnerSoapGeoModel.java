package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 架上皂叠层：复用 {@code soap_bar} 磨损 geo 与颜料贴图。 */
public final class SoapRackInnerSoapGeoModel extends GeoModel<SoapRackBlockEntity> {

    private SoapBarAppearance soap(SoapRackBlockEntity animatable) {
        return animatable.containedSoap();
    }

    @Override
    public ResourceLocation getModelResource(SoapRackBlockEntity animatable) {
        return soap(animatable).modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapRackBlockEntity animatable) {
        return soap(animatable).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapRackBlockEntity animatable) {
        return soap(animatable).animationLocation();
    }
}
