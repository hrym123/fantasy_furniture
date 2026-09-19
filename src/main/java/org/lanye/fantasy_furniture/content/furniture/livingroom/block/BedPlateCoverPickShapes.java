package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.List;
import net.minecraft.world.phys.AABB;

/**
 * 被套选取：各床板被套的平整被面（北向，已做 Gecko X 镜像）。
 *
 * <p>斜折边的方块外包会高出被面，描边变成中间低、两边高。这里只留未旋转的被面：床身一块、床头折起一块。
 * 禁止先 {@code Shapes.or} 再 {@code toAabbs}，床头负 z 要单独留着，裁进床头格。
 */
public final class BedPlateCoverPickShapes {

    private BedPlateCoverPickShapes() {}

    /** {@code bed_plate1_duvet_cover.geo.json} 被面。床头 y 到 12.5，床身 y 到 10.7。 */
    public static List<AABB> plate1() {
        return List.of(
                b(-14.00, 6.54, -7.00, 13.70, 12.54, -1.00),
                b(-14.13, 7.00, -1.00, 14.47, 10.70, 14.00));
    }

    /** {@code bed_plate2_duvet_cover.geo.json} 被面。床头 y 到 12，床身 y 到 11。 */
    public static List<AABB> plate2() {
        return List.of(
                b(1.00, 9.00, -10.00, 14.00, 12.00, -4.00),
                b(1.00, 9.00, -4.00, 14.00, 11.00, 15.00));
    }

    /** {@code bed_plate3_duvet_cover.geo.json} 被面。床头 y 到 10.3，床身 y 到 9.4。 */
    public static List<AABB> plate3() {
        return List.of(
                b(0.00, 4.50, -8.00, 16.00, 10.27, -2.00),
                b(0.00, 4.50, -2.00, 16.00, 9.35, 14.00));
    }

    /** {@code bed_plate4_duvet_cover.geo.json} 被面。床头 y 到 9.8，床身 y 到 8.5。 */
    public static List<AABB> plate4() {
        return List.of(
                b(0.00, 3.00, -8.00, 16.00, 9.77, -1.90),
                b(0.00, 3.00, -2.00, 16.00, 8.50, 16.10));
    }

    /** 像素盒 → 方块单位。调用方按格裁切后再 {@code Shapes.or}。 */
    private static AABB b(double x0, double y0, double z0, double x1, double y1, double z1) {
        return new AABB(x0 / 16.0, y0 / 16.0, z0 / 16.0, x1 / 16.0, y1 / 16.0, z1 / 16.0);
    }
}
