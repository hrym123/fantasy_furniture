package org.lanye.fantasy_furniture.content.furniture.common.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.furniture.common.item.DecorativeHelmetItem;
import software.bernie.geckolib.model.GeoModel;

/** 装饰头饰 GeckoLib 模型（头戴与 BER 共用 {@link DecorativeHelmetItem#assets()}）。 */
public final class DecorativeHelmetGeoModel extends GeoModel<DecorativeHelmetItem> {

    @Override
    public ResourceLocation getModelResource(DecorativeHelmetItem object) {
        return object.assets().model();
    }

    @Override
    public ResourceLocation getTextureResource(DecorativeHelmetItem object) {
        return object.assets().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(DecorativeHelmetItem animatable) {
        return animatable.assets().animation();
    }
}
