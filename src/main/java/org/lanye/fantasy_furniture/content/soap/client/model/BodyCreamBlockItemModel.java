package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamItemRenderState;
import org.lanye.fantasy_furniture.content.soap.item.BodyCreamBlockItem;
import software.bernie.geckolib.model.GeoModel;

public final class BodyCreamBlockItemModel extends GeoModel<BodyCreamBlockItem> {

    @Override
    public ResourceLocation getModelResource(BodyCreamBlockItem animatable) {
        return BodyCreamAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(BodyCreamBlockItem animatable) {
        return BodyCreamItemRenderState.appearance().textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(BodyCreamBlockItem animatable) {
        return BodyCreamAssets.singleAnimationLocation();
    }
}
