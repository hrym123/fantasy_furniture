package org.lanye.fantasy_furniture.geolib;

import net.minecraft.resources.ResourceLocation;

/**
 * GeckoLib 物品（手中 / GUI）渲染用的三套资源，与 {@link GeolibBlockItem}、
 * {@link org.lanye.fantasy_furniture.client.model.GeolibBlockItemModel} 配合。
 * <p>
 * 与 {@link software.bernie.geckolib.model.DefaultedBlockGeoModel} 常用约定一致时可用
 * {@link #blockAsset(String, String)}。
 */
public record GeolibItemAssets(ResourceLocation model, ResourceLocation texture, ResourceLocation animation) {

    /**
     * 与 {@code geo/block/&lt;basename&gt;.geo.json}、{@code textures/block/&lt;basename&gt;.png}、
     * {@code animations/block/&lt;basename&gt;.animation.json} 对齐。
     */
    public static GeolibItemAssets blockAsset(String modid, String basename) {
        return new GeolibItemAssets(
                ResourceLocation.fromNamespaceAndPath(modid, "geo/block/" + basename + ".geo.json"),
                ResourceLocation.fromNamespaceAndPath(modid, "textures/block/" + basename + ".png"),
                ResourceLocation.fromNamespaceAndPath(modid, "animations/block/" + basename + ".animation.json"));
    }
}
