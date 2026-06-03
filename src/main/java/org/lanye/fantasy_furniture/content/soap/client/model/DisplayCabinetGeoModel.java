package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.block.DisplayCabinetBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 陈列柜方块 geo：开闭 + 关盒瓶罐摆放变体。 */
public final class DisplayCabinetGeoModel extends GeoModel<DisplayCabinetBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DisplayCabinetBlockEntity animatable) {
        return DisplayCabinetAssets.modelLocation(animatable);
    }

    @Override
    public ResourceLocation getTextureResource(DisplayCabinetBlockEntity animatable) {
        var state = animatable.getBlockState();
        boolean open = state.getValue(DisplayCabinetBlock.OPEN);
        int material = state.getValue(DisplayCabinetBlock.MATERIAL);
        return DisplayCabinetAssets.textureLocation(open, material);
    }

    @Override
    public ResourceLocation getAnimationResource(DisplayCabinetBlockEntity animatable) {
        return DisplayCabinetAssets.animationLocation();
    }
}
