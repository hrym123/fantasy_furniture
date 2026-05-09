package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.util.Mth;

/**
 * 普通玻璃窗 {@code shape} 与 MoonStarfish bbmodel / 导出资源 basename 对应（{@code geo/block/<basename>.geo.json}）。
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

    private PlainGlassWindowShapes() {}

    public static boolean isValid(int id) {
        return id >= 0 && id < COUNT;
    }

    public static String geoBasename(int shapeId) {
        return GEO_BASENAMES[Mth.clamp(shapeId, 0, COUNT - 1)];
    }
}
