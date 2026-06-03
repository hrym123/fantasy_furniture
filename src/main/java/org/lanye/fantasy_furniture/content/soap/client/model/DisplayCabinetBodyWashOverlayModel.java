package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.DisplayCabinetBottleRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 陈列柜内沐浴露：{@code display_cabinet_wash2.geo.json}（{@code 陈列柜_沐浴露x2.bbmodel}）+ 沐浴露贴图。 */
public final class DisplayCabinetBodyWashOverlayModel extends GeoModel<DisplayCabinetBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DisplayCabinetBlockEntity animatable) {
        return DisplayCabinetAssets.wash2ModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(DisplayCabinetBlockEntity animatable) {
        return BodyWashAssets.textureLocation(DisplayCabinetBottleRenderState.bottleMaterial());
    }

    @Override
    public ResourceLocation getAnimationResource(DisplayCabinetBlockEntity animatable) {
        return DisplayCabinetAssets.animationLocation();
    }
}
