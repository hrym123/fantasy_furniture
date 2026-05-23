package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 肥皂架架体 geo（单贴图）。 */
public final class SoapRackBodyGeoModel extends GeoModel<SoapRackBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapRackBlockEntity animatable) {
        return SoapRackAssets.MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SoapRackBlockEntity animatable) {
        return SoapRackAssets.TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SoapRackBlockEntity animatable) {
        return SoapRackAssets.ANIMATION;
    }
}
