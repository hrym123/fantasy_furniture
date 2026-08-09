package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 方案 3 组合模型档 {@code soap_rack_combo_cream2}。 */
public final class SoapRackComboCream2GeoModel extends GeoModel<SoapRackBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapRackBlockEntity animatable) {
        return SoapRackAssets.COMBO_CREAM2_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SoapRackBlockEntity animatable) {
        return SoapRackAssets.COMBO_CREAM2_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SoapRackBlockEntity animatable) {
        return SoapRackAssets.ANIMATION;
    }
}
