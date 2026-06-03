package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.ShampooStackRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 多瓶堆叠 geo（源自 {@code 洗发露_堆叠_x4.bbmodel}）；贴图由 {@link ShampooStackRenderState} 按层注入。 */
public final class ShampooStackGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        return ShampooAssets.stackModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        return new ShampooAppearance(ShampooStackRenderState.layerMaterial()).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return ShampooAssets.stackAnimationLocation();
    }
}
