package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAssets;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;
import software.bernie.geckolib.model.GeoModel;

/** 地上单只空盒 geo（完整态 / 撕开态）。 */
public final class SoapPaperBoxSingleGeoModel extends GeoModel<SoapPaperBoxBlockEntity> {

    private static SoapPaperBoxAppearance appearance(SoapPaperBoxBlockEntity entity) {
        return new SoapPaperBoxAppearance(entity.materialAtLayer(0));
    }

    @Override
    public ResourceLocation getModelResource(SoapPaperBoxBlockEntity animatable) {
        BlockState state = animatable.getBlockState();
        if (state.getBlock() instanceof SoapPaperBoxBlock block && state.getValue(block.TORN)) {
            return SoapPaperBoxAssets.singleTornModelLocation();
        }
        return SoapPaperBoxAssets.singleModelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapPaperBoxBlockEntity animatable) {
        return appearance(animatable).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapPaperBoxBlockEntity animatable) {
        return SoapPaperBoxAssets.singleAnimationLocation();
    }
}
