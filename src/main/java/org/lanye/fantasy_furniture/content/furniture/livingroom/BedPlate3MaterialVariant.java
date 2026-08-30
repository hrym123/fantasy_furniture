package org.lanye.fantasy_furniture.content.furniture.livingroom;

import net.minecraft.util.StringRepresentable;

/**
 * 床板3型材质档：与 {@code 床板3.bbmodel} 内嵌 beige / white 贴图一一对应。
 */
public enum BedPlate3MaterialVariant implements StringRepresentable {
    BEIGE("beige", "米色"),
    WHITE("white", "白色");

    public static final BedPlate3MaterialVariant[] VALUES = values();
    public static final BedPlate3MaterialVariant DEFAULT = BEIGE;

    private final String id;
    private final String labelZh;

    BedPlate3MaterialVariant(String id, String labelZh) {
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
        return "bed_plate3_" + id;
    }

    public String textureStem() {
        return "bed_plate3_" + id;
    }
}
