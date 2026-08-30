package org.lanye.fantasy_furniture.content.furniture.livingroom;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 床板1型材质档资源：共用 geo {@code bed_plate1}，贴图随档。 */
public final class BedPlate1Materials {

    public static final String GEO_STEM = "bed_plate1";

    private BedPlate1Materials() {}

    public static ResourceLocation texture(BedPlate1MaterialVariant variant) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + variant.textureStem() + ".png");
    }

    public static ResourceLocation geo() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + GEO_STEM + ".geo.json");
    }
}
