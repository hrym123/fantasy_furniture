package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleComboCreamRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 完成态第 3 位：单瓶乳霜 geo + {@link org.lanye.fantasy_furniture.content.soap.SoapComboLayouts} 偏移。 */
public final class SoapBottleCreamStandaloneGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        return BodyCreamAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        return new BodyCreamAppearance(SoapBottleComboCreamRenderState.materialId()).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return BodyCreamAssets.singleAnimationLocation();
    }
}
