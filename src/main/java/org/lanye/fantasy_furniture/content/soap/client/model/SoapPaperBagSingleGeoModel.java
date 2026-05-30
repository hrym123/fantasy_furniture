package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.SoapPackagingAssets;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 地上单只空袋 geo（完整态 / 撕开态）。 */
public final class SoapPaperBagSingleGeoModel extends GeoModel<SoapPaperBagBlockEntity> {

    private static SoapPaperBagAppearance appearance(SoapPaperBagBlockEntity entity) {
        return new SoapPaperBagAppearance(entity.materialAtLayer(0));
    }

    @Override
    public ResourceLocation getModelResource(SoapPaperBagBlockEntity animatable) {
        BlockState state = animatable.getBlockState();
        if (state.getBlock() instanceof SoapPaperBagBlock block && state.getValue(block.TORN)) {
            return SoapPackagingAssets.bagTornModelLocation();
        }
        return appearance(animatable).placedSingleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBagBlockEntity animatable) {
        return appearance(animatable).handheldTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBagBlockEntity animatable) {
        return appearance(animatable).animationLocation();
    }
}
