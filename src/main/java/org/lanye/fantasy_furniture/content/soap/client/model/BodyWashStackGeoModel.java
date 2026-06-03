package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashStackRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 多瓶堆叠 geo（源自 {@code 沐浴露_堆叠_x4.bbmodel}）；贴图由 {@link BodyWashStackRenderState} 按层注入。 */
public final class BodyWashStackGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        return BodyWashAssets.stackModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        return new BodyWashAppearance(BodyWashStackRenderState.layerMaterial()).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return BodyWashAssets.stackAnimationLocation();
    }
}
