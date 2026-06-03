package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.client.DisplayCabinetItemRenderState;
import org.lanye.fantasy_furniture.content.soap.item.DisplayCabinetBlockItem;
import software.bernie.geckolib.model.GeoModel;

public final class DisplayCabinetBlockItemModel extends GeoModel<DisplayCabinetBlockItem> {

    @Override
    public ResourceLocation getModelResource(DisplayCabinetBlockItem animatable) {
        return DisplayCabinetAssets.closedModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(DisplayCabinetBlockItem animatable) {
        return DisplayCabinetItemRenderState.appearance().textureLocation(false);
    }

    @Override
    public ResourceLocation getAnimationResource(DisplayCabinetBlockItem animatable) {
        return DisplayCabinetAssets.animationLocation();
    }
}
