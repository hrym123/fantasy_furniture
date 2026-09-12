package org.lanye.fantasy_furniture.content.furniture.cabinet.state;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 柜子1型竖向拼装角色（邻接自动）：单独 / 柱底 / 柱中 / 柱顶。
 * 柜子2型恒为 {@link #ALONE}。
 */
public enum CabinetSegment implements StringRepresentable {
    ALONE,
    BOTTOM,
    MIDDLE,
    TOP;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 对应导出 geo stem（不含 {@code .geo.json}）；柜子2型仍用 {@code cabinet_2}。 */
    public String cabinet1GeoStem() {
        return switch (this) {
            case ALONE -> "cabinet_1_cell";
            case BOTTOM -> "cabinet_1_2x";
            case MIDDLE -> "cabinet_1_2z";
            case TOP -> "cabinet_1_2s";
        };
    }
}
