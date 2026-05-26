package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBagItemRenderState;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBagBlockItem;
import software.bernie.geckolib.model.GeoModel;

/** 手持空袋 geo × 袋色贴图。 */
public final class SoapPaperBagBlockItemModel extends GeoModel<SoapPaperBagBlockItem> {

    @Override
    public ResourceLocation getModelResource(SoapPaperBagBlockItem animatable) {
        return SoapPaperBagItemRenderState.get().handheldModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBagBlockItem animatable) {
        return SoapPaperBagItemRenderState.get().handheldTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBagBlockItem animatable) {
        return SoapPaperBagAppearance.defaults().animationLocation();
    }
}
