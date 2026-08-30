package org.lanye.fantasy_furniture.content.furniture.livingroom;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 床板3型材质档资源：共用 geo {@code bed_plate3}，贴图随档。 */
public final class BedPlate3Materials {

    public static final String GEO_STEM = "bed_plate3";

    private BedPlate3Materials() {}

    public static ResourceLocation texture(BedPlate3MaterialVariant variant) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + variant.textureStem() + ".png");
    }
}
