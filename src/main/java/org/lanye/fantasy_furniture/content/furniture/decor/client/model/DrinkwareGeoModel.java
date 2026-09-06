package org.lanye.fantasy_furniture.content.furniture.decor.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.DrinkwareBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.DrinkwareBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 杯具方块 geo：按 {@link DrinkwareBlock#STACK} 换模型，按 {@link DrinkwareBlock#MATERIAL} 换贴图。 */
public final class DrinkwareGeoModel extends GeoModel<DrinkwareBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DrinkwareBlockEntity animatable) {
        String id = DrinkwareCollisionShapes.assetId(stackOf(animatable));
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DrinkwareBlockEntity animatable) {
        return DrinkwareMaterials.textureLocation(materialOf(animatable));
    }

    @Override
    public ResourceLocation getAnimationResource(DrinkwareBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");
    }

    private static int stackOf(DrinkwareBlockEntity animatable) {
        var state = animatable.getBlockState();
        if (state.hasProperty(DrinkwareBlock.STACK)) {
            return state.getValue(DrinkwareBlock.STACK);
        }
        return 1;
    }

    private static int materialOf(DrinkwareBlockEntity animatable) {
        var state = animatable.getBlockState();
        if (state.hasProperty(DrinkwareBlock.MATERIAL)) {
            return state.getValue(DrinkwareBlock.MATERIAL);
        }
        return DrinkwareMaterials.DEFAULT;
    }
}
