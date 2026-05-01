package org.lanye.fantasy_furniture.core.geolib;

import net.minecraft.resources.ResourceLocation;

/**
 * GeckoLib 物品（手中 / GUI）渲染用的三套资源，与 {@link GeolibBlockItem}、
 * {@link org.lanye.fantasy_furniture.content.furniture.common.client.model.GeolibBlockItemModel} 配合。
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

    /**
     * 与 {@code geo/item/&lt;basename&gt;.geo.json}、{@code textures/item/&lt;basename&gt;.png}、
     * {@code animations/item/&lt;basename&gt;.animation.json} 对齐（手持 Gecko 物品）。
     */
    public static GeolibItemAssets itemAsset(String modid, String basename) {
        return new GeolibItemAssets(
                ResourceLocation.fromNamespaceAndPath(modid, "geo/item/" + basename + ".geo.json"),
                ResourceLocation.fromNamespaceAndPath(modid, "textures/item/" + basename + ".png"),
                ResourceLocation.fromNamespaceAndPath(modid, "animations/item/" + basename + ".animation.json"));
    }

    /**
     * Geo 使用独立 atlas 贴图（与 {@code item/generated} 的图标 PNG 分离），路径：
     * {@code geo/item/&lt;basename&gt;}、{@code textures/item/&lt;atlasBasename&gt;}、{@code animations/item/&lt;basename&gt;}。
     */
    public static GeolibItemAssets itemGeoAtlas(String modid, String basename, String atlasBasename) {
        return new GeolibItemAssets(
                ResourceLocation.fromNamespaceAndPath(modid, "geo/item/" + basename + ".geo.json"),
                ResourceLocation.fromNamespaceAndPath(modid, "textures/item/" + atlasBasename + ".png"),
                ResourceLocation.fromNamespaceAndPath(modid, "animations/item/" + basename + ".animation.json"));
    }
}
