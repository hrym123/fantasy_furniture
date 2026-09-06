package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 杯具材质档（九色）：与 bbmodel {@code *_cup_1} 贴图槽一一对应。
 */
public final class DrinkwareMaterials {

    public static final int COUNT = 9;
    public static final int DEFAULT = 1;

    /** 创造栏 / 刷子循环序（与 bbmodel 槽 0～8 一致）。 */
    public static final String[] COLOR_KEYS = {
        "green",
        "blue",
        "pink",
        "yellow",
        "purple",
        "dark_blue",
        "red",
        "platinum",
        "orange"
    };

    private DrinkwareMaterials() {}

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
        return "drinkware.fantasy_furniture.color." + colorKey(materialId);
    }

    /** 方块贴图：{@code textures/block/drinkware_{color}.png}。 */
    public static ResourceLocation textureLocation(int materialId) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/drinkware_" + colorKey(materialId) + ".png");
    }
}
