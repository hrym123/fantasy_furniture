package org.lanye.fantasy_furniture.content.furniture.decor.series;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.multiblock.WallPlaneFootprint;

/**
 * 1～7 号窗户规格表。色表跟 moonstarfish 源槽（七色；7 号三色）。
 *
 * <p>占地为墙面宽×高（深 1）；整模北向碰撞在足迹坐标系（底左原点），含 geoOffset 平移。
 */
public final class StyledWindowSeriesCatalog {

    private static final String[] COLORS_7 = {
        "brown", "dark_green", "white", "black", "light_blue", "light_green", "light_yellow"
    };

    private static final String[] COLORS_7_ONLY = {"brown", "white", "black"};

    private static final Map<StyledWindowSeriesId, StyledWindowSeriesSpec> BY_ID = build();

    private StyledWindowSeriesCatalog() {}

    private static Map<StyledWindowSeriesId, StyledWindowSeriesSpec> build() {
        Map<StyledWindowSeriesId, StyledWindowSeriesSpec> m = new EnumMap<>(StyledWindowSeriesId.class);
        m.put(
                StyledWindowSeriesId.W1,
                spec(
                        StyledWindowSeriesId.W1,
                        "1",
                        COLORS_7,
                        WallPlaneFootprint.of(1, 2),
                        0,
                        0,
                        shapes_1(),
                        geos("1", "closed", "22p5", "45", "67p5"),
                        "1号窗户α",
                        "Window No.1α"));
        m.put(
                StyledWindowSeriesId.W1_5,
                spec(
                        StyledWindowSeriesId.W1_5,
                        "1",
                        COLORS_7,
                        WallPlaneFootprint.of(1, 2),
                        0,
                        0,
                        shapes_1_5(),
                        geos("1_5", "closed", "22p5", "45", "67p5"),
                        "1号窗户β",
                        "Window No.1β"));
        m.put(
                StyledWindowSeriesId.W2,
                spec(
                        StyledWindowSeriesId.W2,
                        "2",
                        COLORS_7,
                        WallPlaneFootprint.of(2, 2),
                        0,
                        0,
                        shapes_2(),
                        geos("2", "closed", "22p5", "45", "67p5"),
                        "2号窗户α",
                        "Window No.2α"));
        m.put(
                StyledWindowSeriesId.W2_5,
                spec(
                        StyledWindowSeriesId.W2_5,
                        "2",
                        COLORS_7,
                        WallPlaneFootprint.of(2, 2),
                        0,
                        0,
                        shapes_2_5(),
                        geos("2_5", "closed", "22p5", "45", "67p5"),
                        "2号窗户β",
                        "Window No.2β"));
        m.put(
                StyledWindowSeriesId.W3,
                spec(
                        StyledWindowSeriesId.W3,
                        "3",
                        COLORS_7,
                        WallPlaneFootprint.of(1, 2),
                        0,
                        0,
                        shapes_3(),
                        geos("3", "0", "1", "2", "3"),
                        "3号窗户",
                        "Window No.3"));
        m.put(
                StyledWindowSeriesId.W4,
                spec(
                        StyledWindowSeriesId.W4,
                        "4",
                        COLORS_7,
                        WallPlaneFootprint.of(2, 2),
                        0,
                        0,
                        shapes_4(),
                        geos("4", "0", "1", "2", "3"),
                        "4号窗户",
                        "Window No.4"));
        m.put(
                StyledWindowSeriesId.W5,
                spec(
                        StyledWindowSeriesId.W5,
                        "5",
                        COLORS_7,
                        WallPlaneFootprint.of(3, 2),
                        1,
                        0,
                        shapes_5(),
                        geos("5", "closed", "22p5", "45", "67p5"),
                        "5号窗户",
                        "Window No.5"));
        m.put(
                StyledWindowSeriesId.W6,
                spec(
                        StyledWindowSeriesId.W6,
                        "6",
                        COLORS_7,
                        WallPlaneFootprint.of(3, 2),
                        1,
                        0,
                        shapes_6(),
                        geos("6", "closed", "22p5", "45", "67p5"),
                        "6号窗户",
                        "Window No.6"));
        m.put(
                StyledWindowSeriesId.W7,
                spec(
                        StyledWindowSeriesId.W7,
                        "7",
                        COLORS_7_ONLY,
                        WallPlaneFootprint.of(3, 3),
                        1,
                        1,
                        shapes_7(),
                        geos("7", "closed"),
                        "7号窗户",
                        "Window No.7"));
        return Map.copyOf(m);
    }

    private static String[] geos(String token, String... suffixes) {
        String[] out = new String[suffixes.length];
        for (int i = 0; i < suffixes.length; i++) {
            out[i] = "styled_window_" + token + "_shape_" + suffixes[i];
        }
        return out;
    }

    private static String[] stems(String texKey, String[] colors) {
        String[] out = new String[colors.length];
        for (int i = 0; i < colors.length; i++) {
            out[i] = "styled_window_" + texKey + "_" + i + "_" + colors[i];
        }
        return out;
    }

    /** 足迹空间：已含 geoOffset；来自 raw AABB。 */
    private static VoxelShape[] shapes_1() {
        return new VoxelShape[] {
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 5.15D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 9.18D)
        };
    }

    private static VoxelShape[] shapes_1_5() {
        return shapes_1();
    }

    private static VoxelShape[] shapes_2() {
        return new VoxelShape[] {
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.22D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D)
        };
    }

    private static VoxelShape[] shapes_2_5() {
        return new VoxelShape[] {
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D),
            Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.0D)
        };
    }

    private static VoxelShape[] shapes_3() {
        VoxelShape s = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 2.2D);
        return new VoxelShape[] {s, s, s, s};
    }

    private static VoxelShape[] shapes_4() {
        VoxelShape s = Block.box(0.0D, 0.0D, 0.0D, 32.0D, 32.0D, 2.2D);
        return new VoxelShape[] {s, s, s, s};
    }

    /** raw (−16,0,6)-(32,32,10) + offsetU=1 → (0,0,6)-(48,32,10) */
    private static VoxelShape[] shapes_5() {
        return new VoxelShape[] {
            Block.box(0.0D, 0.0D, 6.0D, 48.0D, 32.0D, 10.0D),
            Block.box(0.0D, 0.0D, 6.0D, 48.0D, 32.0D, 16.0D),
            Block.box(0.0D, 0.0D, 6.0D, 48.0D, 32.0D, 16.0D),
            Block.box(0.0D, 0.0D, 6.0D, 48.0D, 32.0D, 10.0D)
        };
    }

    private static VoxelShape[] shapes_6() {
        return shapes_5();
    }

    /** raw (−16,−16,7)-(32,32,9) + offset (1,1) → (0,0,7)-(48,48,9) */
    private static VoxelShape[] shapes_7() {
        return new VoxelShape[] {Block.box(0.0D, 0.0D, 7.0D, 48.0D, 48.0D, 9.0D)};
    }

    private static StyledWindowSeriesSpec spec(
            StyledWindowSeriesId id,
            String texKey,
            String[] colors,
            WallPlaneFootprint footprint,
            int geoOffsetU,
            int geoOffsetV,
            VoxelShape[] shapes,
            String[] geos,
            String zh,
            String en) {
        return new StyledWindowSeriesSpec(
                id,
                colors,
                stems(texKey, colors),
                geos,
                shapes,
                footprint,
                geoOffsetU,
                geoOffsetV,
                zh,
                en);
    }

    public static StyledWindowSeriesSpec get(StyledWindowSeriesId id) {
        return BY_ID.get(id);
    }

    public static Iterable<StyledWindowSeriesSpec> all() {
        return BY_ID.values();
    }

    /** BlockState {@code shape} 轴上界（含）；全系列统一 0..3，7 号仅用 0。 */
    public static final int SHAPE_PROPERTY_MAX = 3;
}
