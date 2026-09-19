package org.lanye.fantasy_furniture.content.furniture.bar;

import net.minecraft.util.StringRepresentable;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 吧台系 18 种贴图档：6 色 × {@code bar_1} 木纹 / {@code bar_2} 白底 / {@code bar_3} 黑底。 */
public enum BarMaterialVariant implements StringRepresentable {
    PINK_BAR_1("pink_bar_1"),
    GREEN_BAR_1("green_bar_1"),
    RED_BAR_1("red_bar_1"),
    BLUE_BAR_1("blue_bar_1"),
    YELLOW_BAR_1("yellow_bar_1"),
    PURPLE_BAR_1("purple_bar_1"),
    PINK_BAR_2("pink_bar_2"),
    GREEN_BAR_2("green_bar_2"),
    RED_BAR_2("red_bar_2"),
    BLUE_BAR_2("blue_bar_2"),
    YELLOW_BAR_2("yellow_bar_2"),
    PURPLE_BAR_2("purple_bar_2"),
    PINK_BAR_3("pink_bar_3"),
    GREEN_BAR_3("green_bar_3"),
    RED_BAR_3("red_bar_3"),
    BLUE_BAR_3("blue_bar_3"),
    YELLOW_BAR_3("yellow_bar_3"),
    PURPLE_BAR_3("purple_bar_3");

    public static final BarMaterialVariant[] VALUES = values();

    private final String stem;

    BarMaterialVariant(String stem) {
        this.stem = stem;
    }

    /** moonstarfish / {@code textures/block} 文件名（无扩展名）。 */
    public String textureStem() {
        return stem;
    }

    /** 注册 id 后缀，如 {@code pink_bar_1}。 */
    @Override
    public String getSerializedName() {
        return stem;
    }

    /** 展示名颜料段（中文），与肥皂系色名对齐。 */
    public String colorLabelZh() {
        String color = stem.substring(0, stem.indexOf("_bar_"));
        return switch (color) {
            case "pink" -> "樱花粉";
            case "green" -> "薄荷绿";
            case "red" -> "樱桃红";
            case "blue" -> "冰蓝色";
            case "yellow" -> "黄油黄";
            case "purple" -> "丁香紫";
            default -> color;
        };
    }

    /** 展示名底材段（中文）。 */
    public String tierLabelZh() {
        return switch (tier()) {
            case 1 -> "木纹";
            case 2 -> "白底";
            case 3 -> "黑底";
            default -> "底材" + tier();
        };
    }

    public int tier() {
        return Character.getNumericValue(stem.charAt(stem.length() - 1));
    }

    public String blockIdPrefix(String furnitureStem) {
        return furnitureStem + "_" + stem;
    }

    public String langKey(String furnitureStem) {
        return "block." + FantasyFurniture.MODID + "." + blockIdPrefix(furnitureStem);
    }
}
