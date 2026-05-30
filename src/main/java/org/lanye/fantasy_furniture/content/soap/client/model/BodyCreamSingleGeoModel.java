package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 单瓶乳霜 geo（源自 {@code 乳霜_默认.bbmodel}）。 */
public final class BodyCreamSingleGeoModel extends GeoModel<BodyCreamBlockEntity> {

    @Override
    public ResourceLocation getModelResource(BodyCreamBlockEntity animatable) {
        return BodyCreamAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(BodyCreamBlockEntity animatable) {
        return new BodyCreamAppearance(animatable.materialAtLayer(0)).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(BodyCreamBlockEntity animatable) {
        return BodyCreamAssets.singleAnimationLocation();
    }
}
