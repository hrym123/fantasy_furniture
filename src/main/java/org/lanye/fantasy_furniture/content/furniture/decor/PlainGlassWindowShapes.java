package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.util.Mth;

/**
 * 普通玻璃窗 {@code shape} 与 MoonStarfish bbmodel / 导出资源 basename 对应（{@code geo/block/<basename>.geo.json}）。
 *
 * <p>索引与 basename 固定对应（方块状态、碰撞、资源路径）；右键切换顺序见 {@link #nextShapeInCycle(int)}。
 */
public final class PlainGlassWindowShapes {

    public static final int COUNT = 5;

    private static final String[] GEO_BASENAMES = {
        "plain_glass_window_shape_straight",
        "plain_glass_window_shape_90",
        "plain_glass_window_shape_22p5",
        "plain_glass_window_shape_45",
        "plain_glass_window_shape_diag45",
    };

    /**
     * 右键循环：正常(0) → 45°(3) → 22.5°(2) → 90°(1) → 斜角 45°(4) → 正常…（值为 {@link #GEO_BASENAMES} 下标）。
     */
    private static final int[] RIGHT_CLICK_CYCLE = {0, 3, 2, 1, 4};

    private PlainGlassWindowShapes() {}

    public static boolean isValid(int id) {
        return id >= 0 && id < COUNT;
    }

    public static String geoBasename(int shapeId) {
        return GEO_BASENAMES[Mth.clamp(shapeId, 0, COUNT - 1)];
    }

    /** 当前 {@code shape} 索引在右键循环中的下一档（非法值时回到循环首项）。 */
    public static int nextShapeInCycle(int currentShapeId) {
        int current = Mth.clamp(currentShapeId, 0, COUNT - 1);
        for (int i = 0; i < RIGHT_CLICK_CYCLE.length; i++) {
            if (RIGHT_CLICK_CYCLE[i] == current) {
                return RIGHT_CLICK_CYCLE[(i + 1) % RIGHT_CLICK_CYCLE.length];
            }
        }
        return RIGHT_CLICK_CYCLE[0];
    }
}
