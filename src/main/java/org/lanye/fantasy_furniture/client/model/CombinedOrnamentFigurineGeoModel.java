package org.lanye.fantasy_furniture.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.entity.CombinedOrnamentBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 组合摆件玩偶子模型：basename {@code combined_ornament_figurine_<variant>}。 */
public final class CombinedOrnamentFigurineGeoModel extends GeoModel<CombinedOrnamentBlockEntity> {

    @Override
    public ResourceLocation getModelResource(CombinedOrnamentBlockEntity entity) {
        String id = entity.getFigurineVariant().id();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/combined_ornament_figurine_" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CombinedOrnamentBlockEntity entity) {
        String id = entity.getFigurineVariant().id();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/combined_ornament_figurine_" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(CombinedOrnamentBlockEntity entity) {
        String id = entity.getFigurineVariant().id();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/combined_ornament_figurine_" + id + ".animation.json");
    }
}
