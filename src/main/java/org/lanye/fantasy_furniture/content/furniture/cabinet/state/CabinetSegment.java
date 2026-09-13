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
        return cabinet1GeoStem(false, false);
    }

    /**
     * @param openNeg 局部 −X 向有同朝向邻柜（开口应朝 −X）
     * @param openPos 局部 +X 向有同朝向邻柜（开口应朝 +X）
     *     <p>选模：Gecko 北向 geo 相对邻接局部有 X 镜像，故 −X 邻用 {@code open_px_*}（文件缺 +X），
     *     +X 邻用 {@code open_*}（文件缺 −X），渲染后开口朝向邻柜。alone 用 {@code *_cell}。
     */
    public String cabinet1GeoStem(boolean openNeg, boolean openPos) {
        if (!openNeg && !openPos) {
            return switch (this) {
                case ALONE -> "cabinet_1_cell";
                case BOTTOM -> "cabinet_1_2x";
                case MIDDLE -> "cabinet_1_2z";
                case TOP -> "cabinet_1_2s";
            };
        }
        if (openNeg && openPos) {
            return switch (this) {
                case ALONE -> "cabinet_1_open_both_cell";
                case BOTTOM -> "cabinet_1_open_both_2x";
                case MIDDLE -> "cabinet_1_open_both_2z";
                case TOP -> "cabinet_1_open_both_2s";
            };
        }
        // 与文件缺侧相反：补偿 Gecko 北向 X 镜像，使开口朝邻柜
        if (openPos) {
            return switch (this) {
                case ALONE -> "cabinet_1_open_cell";
                case BOTTOM -> "cabinet_1_open_2x";
                case MIDDLE -> "cabinet_1_open_2z";
                case TOP -> "cabinet_1_open_2s";
            };
        }
        return switch (this) {
            case ALONE -> "cabinet_1_open_px_cell";
            case BOTTOM -> "cabinet_1_open_px_2x";
            case MIDDLE -> "cabinet_1_open_px_2z";
            case TOP -> "cabinet_1_open_px_2s";
        };
    }
}
