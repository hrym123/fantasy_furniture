package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBagStackRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 摞体 geo；贴图由 {@link SoapPaperBagStackRenderState} 按层注入。 */
public final class SoapPaperBagStackGeoModel extends GeoModel<SoapPaperBagBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapPaperBagBlockEntity animatable) {
        return SoapPaperBagAppearance.defaults().stackModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBagBlockEntity animatable) {
        return new SoapPaperBagAppearance(SoapPaperBagStackRenderState.layerMaterial()).stackTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBagBlockEntity animatable) {
        return SoapPaperBagAppearance.defaults().animationLocation();
    }
}
