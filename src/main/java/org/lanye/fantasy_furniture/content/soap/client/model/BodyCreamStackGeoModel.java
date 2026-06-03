package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 多瓶堆叠 geo（源自 {@code 乳霜_堆叠_x5.bbmodel}）；贴图由 {@link BodyCreamStackRenderState} 按层注入。 */
public final class BodyCreamStackGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        return BodyCreamAssets.stackModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        return new BodyCreamAppearance(BodyCreamStackRenderState.layerMaterial()).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return BodyCreamAssets.stackAnimationLocation();
    }
}
