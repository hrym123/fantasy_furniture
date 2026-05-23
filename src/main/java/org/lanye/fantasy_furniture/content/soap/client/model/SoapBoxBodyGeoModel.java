package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 肥皂盒盒体：关盖 / 开盖 geo × 六色贴图。 */
public final class SoapBoxBodyGeoModel extends GeoModel<SoapBoxBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBoxBlockEntity animatable) {
        boolean open = animatable.getBlockState().getValue(SoapBoxBlock.OPEN);
        return SoapBoxAppearance.fromState(animatable.getBlockState()).boxModelLocation(open);
    }

    @Override
    public ResourceLocation getTextureResource(SoapBoxBlockEntity animatable) {
        boolean open = animatable.getBlockState().getValue(SoapBoxBlock.OPEN);
        return SoapBoxAppearance.fromState(animatable.getBlockState()).boxTextureLocation(open);
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBoxBlockEntity animatable) {
        return SoapBoxAppearance.defaults().animationLocation();
    }
}
