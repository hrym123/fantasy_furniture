package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 组合态架/盒内皂：与独立件同一套 inner_soap geo。 */
public final class SoapBottleCarrierInnerSoapGeoModel extends GeoModel<SoapBottleBlockEntity> {

    private SoapBarAppearance soap() {
        SoapBarAppearance appearance = SoapBottleCarrierRenderState.soap();
        return appearance != null ? appearance : SoapBarAppearance.defaults();
    }

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        SoapBarAppearance soap = soap();
        if (SoapBottleCarrierRenderState.kind() == SoapStackCarrierKind.BOX) {
            return soap.soapBoxInnerModelLocation();
        }
        return soap.soapRackInnerModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        return soap().textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return soap().animationLocation();
    }
}
