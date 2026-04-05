package org.example.lanye.fantasy_furniture.client.model;

import net.minecraft.resources.ResourceLocation;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.BanquetteBlock;
import org.example.lanye.fantasy_furniture.block.entity.BanquetteBlockEntity;
import org.example.lanye.fantasy_furniture.block.state.BanquetteShape;
import software.bernie.geckolib.model.GeoModel;

/**
 * 卡座：直段 / 拐角 basename 切换；纹理恒为 {@code banquette.png}。
 */
public final class BanquetteBlockGeoModel extends GeoModel<BanquetteBlockEntity> {

    private static String basename(BanquetteBlockEntity entity) {
        BanquetteShape shape = entity.getBlockState().getValue(BanquetteBlock.SHAPE);
        return shape == BanquetteShape.CORNER_LEFT || shape == BanquetteShape.CORNER_RIGHT
                ? "banquette_corner"
                : "banquette_straight";
    }

    @Override
    public ResourceLocation getModelResource(BanquetteBlockEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(
                Fantasy_furniture.MODID, "geo/block/" + basename(entity) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BanquetteBlockEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Fantasy_furniture.MODID, "textures/block/banquette.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BanquetteBlockEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(
                Fantasy_furniture.MODID, "animations/block/" + basename(entity) + ".animation.json");
    }
}
