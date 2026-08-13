package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import software.bernie.geckolib.model.GeoModel;

/**
 * 瓶罐摞载体：单件架 / 关盖盒 geo（位姿由 {@link org.lanye.reverie_core.composite.OffsetGeoPartLayer} 施加）。
 */
public final class SoapBottleCarrierStandaloneGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        SoapStackCarrierKind kind = SoapBottleCarrierRenderState.kind();
        if (kind == SoapStackCarrierKind.BOX) {
            return new SoapBoxAppearance(SoapBottleCarrierRenderState.boxMaterialId())
                    .boxModelLocation(false);
        }
        return SoapRackAssets.MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        SoapStackCarrierKind kind = SoapBottleCarrierRenderState.kind();
        if (kind == SoapStackCarrierKind.BOX) {
            return new SoapBoxAppearance(SoapBottleCarrierRenderState.boxMaterialId())
                    .boxTextureLocation(false);
        }
        return SoapRackAssets.TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return SoapRackAssets.ANIMATION;
    }
}
