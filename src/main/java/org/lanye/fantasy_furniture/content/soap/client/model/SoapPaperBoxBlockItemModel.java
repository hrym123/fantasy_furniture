package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAssets;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBoxItemRenderState;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBoxBlockItem;
import software.bernie.geckolib.model.GeoModel;

public final class SoapPaperBoxBlockItemModel extends GeoModel<SoapPaperBoxBlockItem> {

    @Override
    public ResourceLocation getModelResource(SoapPaperBoxBlockItem animatable) {
        return SoapPaperBoxAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBoxBlockItem animatable) {
        return SoapPaperBoxItemRenderState.appearance().textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBoxBlockItem animatable) {
        return SoapPaperBoxAssets.singleAnimationLocation();
    }
}
