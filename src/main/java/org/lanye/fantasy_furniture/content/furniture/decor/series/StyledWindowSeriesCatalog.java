package org.lanye.fantasy_furniture.content.furniture.decor.series;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.multiblock.WallPlaneFootprint;

/**
 * 1～7 号窗户规格表。色表跟 moonstarfish 源槽（七色；7 号三色）。
 *
 * <p>占地为墙面宽×高（深 1）；整模北向碰撞在足迹坐标系（底左原点），含 geoOffset 平移。
 * 玩法碰撞为足迹外接薄盒（按格切片）：1～4 号 Z 跟 2 号窗（0～2.0）；5～7 号保留 geo 内缩 AABB。
 * 各档 {@code shapes_*} 仅 shape 0 参与玩法；开档条目与关档同盒（造型只切 Geo）。
 * 1/2 号 α 底部挡板碰撞外探（负 Z），不扩大 {@link WallPlaneFootprint}。
 */
public final class StyledWindowSeriesCatalog {

    /** 玩法碰撞 Z 上界（跟 2 号窗薄片一致，单位：1/16 格）。 */
    private static final double WINDOW_COLLISION_Z = 2.0D;

    /**
     * 1/2 号 α 底部挡板：geo z=-17～-6 → gecko 后 z=-9～2；高 1。
     * 负 Z 探出足迹格，实体可挡；不占墙面多格副格。
     */
    private static final double SILL_Z_MIN = -9.0D;
    private static final double SILL_Y_MAX = 1.0D;

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

    /** 足迹空间单 AABB；供 {@link org.lanye.reverie_core.geolib.multiblock.WallPlanePlacement#sliceNorthToPart} 切片。 */
    private static VoxelShape windowCollisionBox(double width, double height, double heightMax) {
        return Block.box(0.0D, 0.0D, 0.0D, width, heightMax, WINDOW_COLLISION_Z);
    }

    private static VoxelShape[] repeatShape(VoxelShape shape, int count) {
        VoxelShape[] out = new VoxelShape[count];
        for (int i = 0; i < count; i++) {
            out[i] = shape;
        }
        return out;
    }

    /** 四档造型共用关档薄片（开合不改玩法盒）。 */
    private static VoxelShape[] windowShapesLikeW2(double width, double height) {
        return repeatShape(windowCollisionBox(width, height, height), 4);
    }

    /** 框体薄片 ∪ 底部挡板（仅 1/2 号 α）。 */
    private static VoxelShape[] windowShapesWithFrontSill(double width, double height) {
        VoxelShape frame = windowCollisionBox(width, height, height);
        VoxelShape sill =
                Block.box(0.0D, 0.0D, SILL_Z_MIN, width, SILL_Y_MAX, WINDOW_COLLISION_Z);
        return repeatShape(Shapes.or(frame, sill), 4);
    }

    private static VoxelShape[] shapes_1() {
        return windowShapesWithFrontSill(16.0D, 32.0D);
    }

    private static VoxelShape[] shapes_1_5() {
        return windowShapesLikeW2(16.0D, 32.0D);
    }

    private static VoxelShape[] shapes_2() {
        return windowShapesWithFrontSill(32.0D, 32.0D);
    }

    private static VoxelShape[] shapes_2_5() {
        return windowShapesLikeW2(32.0D, 32.0D);
    }

    private static VoxelShape[] shapes_3() {
        return windowShapesLikeW2(16.0D, 32.0D);
    }

    private static VoxelShape[] shapes_4() {
        return windowShapesLikeW2(32.0D, 32.0D);
    }

    /** raw (16,0,6)-(32,32,10) + offsetU=1 → (0,0,6)-(48,32,10) */
    private static VoxelShape[] shapes_5() {
        return repeatShape(Block.box(0.0D, 0.0D, 6.0D, 48.0D, 32.0D, 10.0D), 4);
    }

    private static VoxelShape[] shapes_6() {
        return shapes_5();
    }

    /** raw (16,16,7)-(32,32,9) + offset (1,1) → (0,0,7)-(48,48,9) */
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
