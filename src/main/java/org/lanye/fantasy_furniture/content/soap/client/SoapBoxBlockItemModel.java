package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;
import software.bernie.geckolib.model.GeoModel;

/** 手持 / 创造栏：关盖空盒 geo，贴图随盒色 NBT。 */
public final class SoapBoxBlockItemModel extends GeoModel<SoapBoxBlockItem> {

    private SoapBoxAppearance appearance() {
        SoapBoxAppearance a = SoapBoxItemRenderer.currentAppearance();
        return a != null ? a : SoapBoxAppearance.defaults();
    }

    @Override
    public ResourceLocation getModelResource(SoapBoxBlockItem animatable) {
        return appearance().closedItemModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBoxBlockItem animatable) {
        return appearance().closedItemTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBoxBlockItem animatable) {
        return appearance().animationLocation();
    }
}
