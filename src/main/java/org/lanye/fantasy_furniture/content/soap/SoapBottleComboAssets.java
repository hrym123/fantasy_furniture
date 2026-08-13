package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 瓶罐组合特殊场合 2/3 专用 Geo（源自 moonstarfish 组合目录；贴图仍复用架/盒/乳霜既有资源）。
 */
public final class SoapBottleComboAssets {

    private SoapBottleComboAssets() {}

    public static final ResourceLocation RACK_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_bottle_combo_rack.geo.json");

    public static final ResourceLocation RACK2_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_bottle_combo_rack2.geo.json");

    public static final ResourceLocation BOX_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_bottle_combo_box.geo.json");

    public static final ResourceLocation BOX2_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_bottle_combo_box2.geo.json");

    public static final ResourceLocation CREAM_MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/soap_bottle_combo_cream.geo.json");

    public static ResourceLocation rackModel(boolean intermediate) {
        return intermediate ? RACK2_MODEL : RACK_MODEL;
    }

    public static ResourceLocation boxModel(boolean intermediate) {
        return intermediate ? BOX2_MODEL : BOX_MODEL;
    }
}
