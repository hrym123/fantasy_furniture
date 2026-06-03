package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.DisplayCabinetBottleRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 陈列柜内洗发露：{@code display_cabinet_shampoo2.geo.json}（{@code 陈列柜_洗发露x2.bbmodel}）+ 洗发露贴图。 */
public final class DisplayCabinetShampooOverlayModel extends GeoModel<DisplayCabinetBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DisplayCabinetBlockEntity animatable) {
        return DisplayCabinetAssets.shampoo2ModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(DisplayCabinetBlockEntity animatable) {
        return ShampooAssets.textureLocation(DisplayCabinetBottleRenderState.bottleMaterial());
    }

    @Override
    public ResourceLocation getAnimationResource(DisplayCabinetBlockEntity animatable) {
        return DisplayCabinetAssets.animationLocation();
    }
}
