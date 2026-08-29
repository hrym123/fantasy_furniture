package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 电脑材质档（六色）：与 bbmodel 内嵌 {@code *_computer_style_*} 贴图一一对应。
 */
public final class ComputerMaterials {

    public static final int COUNT = 6;
    public static final int DEFAULT = 1;

    /** 创造栏 / 刷子循环序：黄 → 绿 → 粉 → 蓝 → 紫 → 红。 */
    public static final String[] COLOR_KEYS = {
        "yellow", "green", "pink", "blue", "purple", "red"
    };

    private ComputerMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    public static int clamp(int materialId) {
        return isValid(materialId) ? materialId : DEFAULT;
    }

    public static String colorKey(int materialId) {
        return COLOR_KEYS[clamp(materialId) - 1];
    }

    public static String colorTranslationKey(int materialId) {
        return "computer.fantasy_furniture.color." + colorKey(materialId);
    }

    /** 方块贴图：{@code textures/block/{closedAssetId}_{color}.png}。 */
    public static ResourceLocation textureLocation(String closedAssetId, int materialId) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID,
                "textures/block/" + closedAssetId + "_" + colorKey(materialId) + ".png");
    }

    public static ResourceLocation geoLocation(String closedAssetId, boolean open) {
        String stem = open ? closedAssetId + "_open" : closedAssetId;
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + stem + ".geo.json");
    }

    public static ResourceLocation animationLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");
    }
}
