package org.lanye.fantasy_furniture.content.furniture.common.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.furniture.common.item.ArcaneWandItem;
import software.bernie.geckolib.model.GeoModel;

/**
 * 魔杖专用 {@link GeoModel}，资源约定与 {@link GeolibHandheldItemModel} 相同（{@link ArcaneWandItem#assets()}）。
 */
public final class ArcaneWandItemModel extends GeoModel<ArcaneWandItem> {

    @Override
    public ResourceLocation getModelResource(ArcaneWandItem object) {
        return object.assets().model();
    }

    @Override
    public ResourceLocation getTextureResource(ArcaneWandItem object) {
        return object.assets().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(ArcaneWandItem animatable) {
        return animatable.assets().animation();
    }
}
