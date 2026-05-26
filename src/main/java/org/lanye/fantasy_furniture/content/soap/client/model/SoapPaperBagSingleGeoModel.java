package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 地上单只空袋 geo（源自 {@code 包装袋_肥皂.bbmodel} 袋体）。 */
public final class SoapPaperBagSingleGeoModel extends GeoModel<SoapPaperBagBlockEntity> {

    private static SoapPaperBagAppearance appearance(SoapPaperBagBlockEntity entity) {
        return new SoapPaperBagAppearance(entity.materialAtLayer(0));
    }

    @Override
    public ResourceLocation getModelResource(SoapPaperBagBlockEntity animatable) {
        return appearance(animatable).placedSingleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBagBlockEntity animatable) {
        return appearance(animatable).handheldTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBagBlockEntity animatable) {
        return appearance(animatable).animationLocation();
    }
}
