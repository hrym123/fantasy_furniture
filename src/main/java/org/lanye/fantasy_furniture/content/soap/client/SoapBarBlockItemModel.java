package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import software.bernie.geckolib.model.GeoModel;

/** 手持 / 创造栏：复用 {@code geo/block/soap_bar*.geo.json}，贴图随堆叠 NBT 变化。 */
public final class SoapBarBlockItemModel extends GeoModel<SoapBarBlockItem> {

    private SoapBarAppearance appearance() {
        SoapBarAppearance a = SoapBarItemRenderer.currentAppearance();
        return a != null ? a : SoapBarAppearance.defaults();
    }

    @Override
    public ResourceLocation getModelResource(SoapBarBlockItem animatable) {
        return appearance().modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapBarBlockItem animatable) {
        return appearance().textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBarBlockItem animatable) {
        return appearance().animationLocation();
    }
}
