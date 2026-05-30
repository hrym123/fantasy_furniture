package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 包装袋 / 包装盒完整态与撕开态 geo（源自 moonstarfish {@code *_肥皂_撕开.bbmodel}）。 */
public final class SoapPackagingAssets {

    private SoapPackagingAssets() {}

    public static ResourceLocation bagIntactModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_bag.geo.json");
    }

    public static ResourceLocation bagTornModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_bag_torn.geo.json");
    }

    public static ResourceLocation boxIntactModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_box.geo.json");
    }

    public static ResourceLocation boxTornModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_box_torn.geo.json");
    }
}
