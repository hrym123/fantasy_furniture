package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.util.StringRepresentable;

/**
 * 楼梯（{@code styled_stairs}）21 材质档：贴图 stem = {@code styled_stairs_<id>}。
 *
 * <p>顺序对齐 moonstarfish {@code 楼梯.bbmodel} 贴图槽。
 */
public enum StyledStairsMaterialVariant implements StringRepresentable {
    ORANGE_RED("orange_red", "橙红"),
    PURPLE_PINK("purple_pink", "紫粉"),
    BLUE_PINK("blue_pink", "蓝粉"),
    GREEN_YELLOW("green_yellow", "绿黄"),
    COLORFUL_1("colorful_1", "彩绘一"),
    COLORFUL_2("colorful_2", "彩绘二"),
    COLORFUL_3("colorful_3", "彩绘三"),
    COLORFUL_4("colorful_4", "彩绘四"),
    COLORFUL_5("colorful_5", "彩绘五"),
    COLORFUL_6("colorful_6", "彩绘六"),
    COLORFUL_7("colorful_7", "彩绘七"),
    COLORFUL_8("colorful_8", "彩绘八"),
    COLORFUL_9("colorful_9", "彩绘九"),
    RED("red", "红色"),
    YELLOW("yellow", "黄色"),
    GREEN("green", "绿色"),
    BLUE("blue", "蓝色"),
    PURPLE("purple", "紫色"),
    PINK("pink", "粉色"),
    BLACK("black", "黑色"),
    WHITE("white", "白色");

    public static final StyledStairsMaterialVariant[] VALUES = values();

    private final String id;
    private final String labelZh;

    StyledStairsMaterialVariant(String id, String labelZh) {
        this.id = id;
        this.labelZh = labelZh;
    }

    /** 注册 id / 贴图后缀。 */
    @Override
    public String getSerializedName() {
        return id;
    }

    public String labelZh() {
        return labelZh;
    }

    /** {@code textures/block/styled_stairs_<id>} */
    public String textureStem() {
        return "styled_stairs_" + id;
    }

    public String blockId() {
        return "styled_stairs_" + id;
    }
}
