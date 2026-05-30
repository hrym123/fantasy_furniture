package org.lanye.fantasy_furniture.content.soap;

import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 肥皂套系 Geo 方块：{@link GeolibItemAssets#blockAssetWithTexture} 约定。 */
public final class SoapSeriesBlockAssets {

    private SoapSeriesBlockAssets() {}

    public static GeolibItemAssets blockPrimaryTexture(String geoBasename) {
        return new GeolibItemAssets(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "geo/block/" + geoBasename + ".geo.json"),
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "textures/block/" + geoBasename + "_1.png"),
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "animations/block/" + geoBasename + ".animation.json"));
    }
}
