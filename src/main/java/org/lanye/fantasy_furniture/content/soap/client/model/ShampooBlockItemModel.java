package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.client.ShampooItemRenderState;
import org.lanye.fantasy_furniture.content.soap.item.ShampooBlockItem;
import software.bernie.geckolib.model.GeoModel;

public final class ShampooBlockItemModel extends GeoModel<ShampooBlockItem> {

    @Override
    public ResourceLocation getModelResource(ShampooBlockItem animatable) {
        return ShampooAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(ShampooBlockItem animatable) {
        return ShampooItemRenderState.appearance().textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(ShampooBlockItem animatable) {
        return ShampooAssets.singleAnimationLocation();
    }
}
