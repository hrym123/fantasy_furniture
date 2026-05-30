package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashItemRenderState;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;
import software.bernie.geckolib.model.GeoModel;

public final class BodyWashBlockItemModel extends GeoModel<BodyWashBlockItem> {

    @Override
    public ResourceLocation getModelResource(BodyWashBlockItem animatable) {
        return BodyWashAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(BodyWashBlockItem animatable) {
        return BodyWashItemRenderState.appearance().textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(BodyWashBlockItem animatable) {
        return BodyWashAssets.singleAnimationLocation();
    }
}
