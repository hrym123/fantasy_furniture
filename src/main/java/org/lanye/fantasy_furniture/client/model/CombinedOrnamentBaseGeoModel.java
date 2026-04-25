package org.lanye.fantasy_furniture.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.entity.CombinedOrnamentBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 组合摆件底座子模型：basename {@code combined_ornament_base_<variant>}。 */
public final class CombinedOrnamentBaseGeoModel extends GeoModel<CombinedOrnamentBlockEntity> {

    @Override
    public ResourceLocation getModelResource(CombinedOrnamentBlockEntity entity) {
        String id = entity.getBaseVariant().id();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/combined_ornament_base_" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CombinedOrnamentBlockEntity entity) {
        String id = entity.getBaseVariant().id();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/combined_ornament_base_" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(CombinedOrnamentBlockEntity entity) {
        String id = entity.getBaseVariant().id();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/combined_ornament_base_" + id + ".animation.json");
    }
}
