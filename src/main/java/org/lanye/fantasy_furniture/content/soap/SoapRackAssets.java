package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 肥皂架架体资源（单件 geo；组合陈列用偏移叠瓶，无组合专用模型档）。 */
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
