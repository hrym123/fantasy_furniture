package org.lanye.fantasy_furniture.content.furniture.livingroom;

import net.minecraft.util.StringRepresentable;

/**
 * 床板1型材质档：与 {@code 床板1.bbmodel} 内嵌 beige / white / black 贴图一一对应。
 */
public enum BedPlate1MaterialVariant implements StringRepresentable {
    BEIGE("beige", "米色"),
    WHITE("white", "白色"),
    BLACK("black", "黑色");

    public static final BedPlate1MaterialVariant[] VALUES = values();
    public static final BedPlate1MaterialVariant DEFAULT = BEIGE;

    private final String id;
    private final String labelZh;

    BedPlate1MaterialVariant(String id, String labelZh) {
        this.id = id;
        this.labelZh = labelZh;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public String labelZh() {
        return labelZh;
    }

    public String blockId() {
        return "bed_plate1_" + id;
    }

    /** {@code textures/block/bed_plate1_<id>} */
    public String textureStem() {
        return "bed_plate1_" + id;
    }
}
