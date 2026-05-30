package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBoxStackRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 包装盒摞 geo；模型按堆叠样式、贴图按层注入。 */
public final class SoapPaperBoxStackGeoModel extends GeoModel<SoapPaperBoxBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapPaperBoxBlockEntity animatable) {
        return SoapPaperBoxAssets.stackModelLocation(SoapPaperBoxStackRenderState.stackStyle());
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBoxBlockEntity animatable) {
        return new SoapPaperBoxAppearance(SoapPaperBoxStackRenderState.layerMaterial()).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBoxBlockEntity animatable) {
        return SoapPaperBoxAssets.stackAnimationLocation();
    }
}
