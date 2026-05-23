package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 肥皂架架体：单 geo、单贴图（{@code soap_rack_1} / Blockbench {@code wooden_soap_dish}）。 */
public final class SoapRackAssets {

    private SoapRackAssets() {}

    public static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "geo/block/soap_rack.geo.json");

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "textures/block/soap_rack_1.png");

    public static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");
}
