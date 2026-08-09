package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 肥皂架架体与组合陈列模型档。 */
public final class SoapRackAssets {

    private SoapRackAssets() {}

    public static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "geo/block/soap_rack.geo.json");

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "textures/block/soap_rack_1.png");

    public static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    /** 方案 1 一体模型（多槽贴图；当前渲染取槽 1，精细分槽后续可补）。 */
    public static final ResourceLocation COMBO_FULL_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_rack_combo_full.geo.json");

    public static final ResourceLocation COMBO_FULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "textures/block/soap_rack_combo_full_1.png");

    /** 方案 3：乳霜 ×2 + 有皂架 */
    public static final ResourceLocation COMBO_CREAM2_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_rack_combo_cream2.geo.json");

    public static final ResourceLocation COMBO_CREAM2_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "textures/block/soap_rack_combo_cream2_1.png");
}
