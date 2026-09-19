package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;

/**
 * 床板1型枕头选取：自组合导出 geo 的北向体素，再按默认 Geo 渲染做 X 镜像（相对床尾右锚点）。
 * 倾斜立方按 Gecko 烘焙：先翻 X，再以 {@code (-rx,-ry,rz)} 绕轴转。勿先转再镜像 AABB。
 * 与床单裁切同一坐标系，不另加厚。
 */
public final class BedPlate1PillowPickShapes {

    private BedPlate1PillowPickShapes() {}

    private static final VoxelShape LARGE_S1 = mirrorGeckoX(largeS1Raw());
    private static final VoxelShape LARGE_S2 = mirrorGeckoX(largeS2Raw());
    private static final VoxelShape LARGE_P1 = mirrorGeckoX(largeP1Raw());
    private static final VoxelShape LARGE_P2 = mirrorGeckoX(largeP2Raw());
    private static final VoxelShape MEDIUM_P1 = mirrorGeckoX(mediumP1Raw());
    private static final VoxelShape MEDIUM_P2 = mirrorGeckoX(mediumP2Raw());
    private static final VoxelShape MEDIUM_S1 = mirrorGeckoX(mediumS1Raw());
    private static final VoxelShape MEDIUM_S2 = mirrorGeckoX(mediumS2Raw());
    private static final VoxelShape MEDIUM_S3 = mirrorGeckoX(shiftPixels(mediumS1Raw(), -8.5, 0.0, 2.0));
    private static final VoxelShape SMALL_PS4 = mirrorGeckoX(smallPs4Raw());
    private static final VoxelShape SMALL_PS5 = mirrorGeckoX(smallPs5Raw());
    private static final VoxelShape SMALL_PS4_COVER = mirrorGeckoX(smallPs4CoverRaw());
    private static final VoxelShape SMALL_PS5_COVER = mirrorGeckoX(smallPs5CoverRaw());

    public static VoxelShape northFor(
            BedPlateSheetPillowSlots slots, BedPlate1CollisionShapes.PickedLayer layer, boolean hasCover) {
        if (slots == null || layer == null) {
            return Shapes.empty();
        }
        return switch (layer) {
            case LARGE_1 -> slots.hasLargeSlot(1) ? (slots.resolvedMode(1) == 0 ? LARGE_P1 : LARGE_S1) : Shapes.empty();
            case LARGE_2 -> slots.hasLargeSlot(2) ? (slots.resolvedMode(1) == 0 ? LARGE_P2 : LARGE_S2) : Shapes.empty();
            case MEDIUM_1 -> medium(slots, 1);
            case MEDIUM_2 -> medium(slots, 2);
            case MEDIUM_3 -> medium(slots, 3);
            case SMALL_4 -> small(slots, 4, hasCover);
            case SMALL_5 -> small(slots, 5, hasCover);
            case BODY, DUVET, DUVET_COVER -> Shapes.empty();
        };
    }

    private static VoxelShape medium(BedPlateSheetPillowSlots slots, int side) {
        if (!slots.hasMediumSlot(side)) {
            return Shapes.empty();
        }
        if (side == 3) {
            return MEDIUM_S3;
        }
        boolean standing = slots.resolvedMode(1) != 0;
        if (side == 2) {
            return standing ? MEDIUM_S2 : MEDIUM_P2;
        }
        return standing ? MEDIUM_S1 : MEDIUM_P1;
    }

    private static VoxelShape small(BedPlateSheetPillowSlots slots, int slot, boolean hasCover) {
        if (!slots.hasSmallSlot(slot)) {
            return Shapes.empty();
        }
        if (slot == 5) {
            return hasCover ? SMALL_PS5_COVER : SMALL_PS5;
        }
        return hasCover ? SMALL_PS4_COVER : SMALL_PS4;
    }

    /** 像素平移，用在 Gecko 镜像之前。中号 S3 相对 S1 的模型原点差。 */
    private static VoxelShape shiftPixels(VoxelShape shape, double dx, double dy, double dz) {
        VoxelShape out = Shapes.empty();
        double sx = dx / 16.0;
        double sy = dy / 16.0;
        double sz = dz / 16.0;
        for (AABB box : shape.toAabbs()) {
            out = Shapes.or(
                    out,
                    Shapes.box(
                            box.minX + sx, box.minY + sy, box.minZ + sz, box.maxX + sx, box.maxY + sy, box.maxZ + sz));
        }
        return out;
    }

    /** 默认 GeoBlockRenderer：模型 +X 在北向画到锚点 -X，选取盒相对导出做 {@code x' = 1 - x}。 */
    private static VoxelShape mirrorGeckoX(VoxelShape exportNorth) {
        VoxelShape out = Shapes.empty();
        for (AABB box : exportNorth.toAabbs()) {
            out = Shapes.or(
                    out,
                    Shapes.box(1.0 - box.maxX, box.minY, box.minZ, 1.0 - box.minX, box.maxY, box.maxZ));
        }
        return out;
    }

    /** {@code bed_plate1_pillow_large_s1.geo.json} sha256[:12]=168e65b0d370；尚未做 Gecko X 镜像。 */
    private static VoxelShape largeS1Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(16.50, 6.94, -11.74, 29.50, 13.49, -8.87));
        s = Shapes.or(s, Block.box(17.50, 6.60, -12.57, 28.50, 13.83, -8.04));
        return s;
    }

    /** {@code bed_plate1_pillow_large_s2.geo.json} sha256[:12]=0b2d8e401587；尚未做 Gecko X 镜像。 */
    private static VoxelShape largeS2Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(0.50, 6.94, -11.74, 13.50, 13.49, -8.87));
        s = Shapes.or(s, Block.box(1.50, 6.60, -12.57, 12.50, 13.83, -8.04));
        return s;
    }

    /** {@code bed_plate1_pillow_large_p1.geo.json} sha256[:12]=dba057cdae47；尚未做 Gecko X 镜像。 */
    private static VoxelShape largeP1Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(15.50, 7.78, -14.00, 28.50, 7.98, -7.00));
        s = Shapes.or(s, Block.box(16.50, 6.88, -14.00, 27.50, 8.88, -7.00));
        return s;
    }

    /** {@code bed_plate1_pillow_large_p2.geo.json} sha256[:12]=b133b1cf32ac；尚未做 Gecko X 镜像。 */
    private static VoxelShape largeP2Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(1.50, 7.78, -14.00, 14.50, 7.98, -7.00));
        s = Shapes.or(s, Block.box(2.50, 6.88, -14.00, 13.50, 8.88, -7.00));
        return s;
    }

    /** {@code bed_plate1_pillow_medium_p1.geo.json} sha256[:12]=5a3f5c477e1c；尚未做 Gecko X 镜像。 */
    private static VoxelShape mediumP1Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(18.00, 7.00, -14.00, 28.00, 9.00, -7.00));
        s = Shapes.or(s, Block.box(16.60, 7.90, -8.00, 18.00, 8.30, -7.00));
        s = Shapes.or(s, Block.box(16.60, 7.70, -11.00, 18.00, 8.10, -10.00));
        s = Shapes.or(s, Block.box(16.60, 7.70, -13.00, 18.00, 8.10, -12.00));
        s = Shapes.or(s, Block.box(16.60, 7.90, -10.00, 18.00, 8.30, -9.00));
        s = Shapes.or(s, Block.box(16.60, 7.90, -14.00, 18.00, 8.30, -13.00));
        s = Shapes.or(s, Block.box(16.60, 7.70, -9.00, 18.00, 8.10, -8.00));
        s = Shapes.or(s, Block.box(16.60, 7.90, -12.00, 18.00, 8.30, -11.00));
        s = Shapes.or(s, Block.box(28.00, 7.90, -8.00, 29.40, 8.30, -7.00));
        s = Shapes.or(s, Block.box(28.00, 7.70, -11.00, 29.40, 8.10, -10.00));
        s = Shapes.or(s, Block.box(28.00, 7.70, -13.00, 29.40, 8.10, -12.00));
        s = Shapes.or(s, Block.box(28.00, 7.90, -10.00, 29.40, 8.30, -9.00));
        s = Shapes.or(s, Block.box(28.00, 7.90, -14.00, 29.40, 8.30, -13.00));
        s = Shapes.or(s, Block.box(28.00, 7.70, -9.00, 29.40, 8.10, -8.00));
        s = Shapes.or(s, Block.box(28.00, 7.90, -12.00, 29.40, 8.30, -11.00));
        return s;
    }

    /** {@code bed_plate1_pillow_medium_p2.geo.json} sha256[:12]=48587f8609b1；尚未做 Gecko X 镜像。 */
    private static VoxelShape mediumP2Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(3.00, 7.00, -14.00, 13.00, 9.00, -7.00));
        s = Shapes.or(s, Block.box(1.60, 7.90, -8.00, 3.00, 8.30, -7.00));
        s = Shapes.or(s, Block.box(1.60, 7.70, -11.00, 3.00, 8.10, -10.00));
        s = Shapes.or(s, Block.box(1.60, 7.70, -13.00, 3.00, 8.10, -12.00));
        s = Shapes.or(s, Block.box(1.60, 7.90, -10.00, 3.00, 8.30, -9.00));
        s = Shapes.or(s, Block.box(1.60, 7.90, -14.00, 3.00, 8.30, -13.00));
        s = Shapes.or(s, Block.box(1.60, 7.70, -9.00, 3.00, 8.10, -8.00));
        s = Shapes.or(s, Block.box(1.60, 7.90, -12.00, 3.00, 8.30, -11.00));
        s = Shapes.or(s, Block.box(13.00, 7.90, -8.00, 14.40, 8.30, -7.00));
        s = Shapes.or(s, Block.box(13.00, 7.70, -11.00, 14.40, 8.10, -10.00));
        s = Shapes.or(s, Block.box(13.00, 7.70, -13.00, 14.40, 8.10, -12.00));
        s = Shapes.or(s, Block.box(13.00, 7.90, -10.00, 14.40, 8.30, -9.00));
        s = Shapes.or(s, Block.box(13.00, 7.90, -14.00, 14.40, 8.30, -13.00));
        s = Shapes.or(s, Block.box(13.00, 7.70, -9.00, 14.40, 8.10, -8.00));
        s = Shapes.or(s, Block.box(13.00, 7.90, -12.00, 14.40, 8.30, -11.00));
        return s;
    }

    /** {@code bed_plate1_pillow_medium_s1.geo.json} sha256[:12]=710adc26db37；尚未做 Gecko X 镜像。 */
    private static VoxelShape mediumS1Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(19.00, 6.62, -12.60, 29.00, 13.85, -8.08));
        s = Shapes.or(s, Block.box(17.60, 6.96, -9.48, 19.00, 8.04, -8.72));
        s = Shapes.or(s, Block.box(17.60, 9.66, -10.81, 19.00, 10.73, -10.06));
        s = Shapes.or(s, Block.box(17.60, 11.50, -11.57, 19.00, 12.58, -10.82));
        s = Shapes.or(s, Block.box(17.60, 8.81, -10.24, 19.00, 9.89, -9.49));
        s = Shapes.or(s, Block.box(17.60, 12.51, -11.77, 19.00, 13.58, -11.02));
        s = Shapes.or(s, Block.box(17.60, 7.81, -10.04, 19.00, 8.89, -9.29));
        s = Shapes.or(s, Block.box(17.60, 10.66, -11.01, 19.00, 11.73, -10.25));
        s = Shapes.or(s, Block.box(29.00, 6.96, -9.48, 30.40, 8.04, -8.72));
        s = Shapes.or(s, Block.box(29.00, 9.66, -10.81, 30.40, 10.73, -10.06));
        s = Shapes.or(s, Block.box(29.00, 11.50, -11.57, 30.40, 12.58, -10.82));
        s = Shapes.or(s, Block.box(29.00, 8.81, -10.24, 30.40, 9.89, -9.49));
        s = Shapes.or(s, Block.box(29.00, 12.51, -11.77, 30.40, 13.58, -11.02));
        s = Shapes.or(s, Block.box(29.00, 7.81, -10.04, 30.40, 8.89, -9.29));
        s = Shapes.or(s, Block.box(29.00, 10.66, -11.01, 30.40, 11.73, -10.25));
        return s;
    }

    /** {@code bed_plate1_pillow_medium_s2.geo.json} sha256[:12]=b30a16576540；尚未做 Gecko X 镜像。 */
    private static VoxelShape mediumS2Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(2.00, 6.62, -12.60, 12.00, 13.85, -8.08));
        s = Shapes.or(s, Block.box(0.60, 6.96, -9.48, 2.00, 8.04, -8.72));
        s = Shapes.or(s, Block.box(0.60, 9.66, -10.81, 2.00, 10.73, -10.06));
        s = Shapes.or(s, Block.box(0.60, 11.50, -11.57, 2.00, 12.58, -10.82));
        s = Shapes.or(s, Block.box(0.60, 8.81, -10.24, 2.00, 9.89, -9.49));
        s = Shapes.or(s, Block.box(0.60, 12.51, -11.77, 2.00, 13.58, -11.02));
        s = Shapes.or(s, Block.box(0.60, 7.81, -10.04, 2.00, 8.89, -9.29));
        s = Shapes.or(s, Block.box(0.60, 10.66, -11.01, 2.00, 11.73, -10.25));
        s = Shapes.or(s, Block.box(12.00, 6.96, -9.48, 13.40, 8.04, -8.72));
        s = Shapes.or(s, Block.box(12.00, 9.66, -10.81, 13.40, 10.73, -10.06));
        s = Shapes.or(s, Block.box(12.00, 11.50, -11.57, 13.40, 12.58, -10.82));
        s = Shapes.or(s, Block.box(12.00, 8.81, -10.24, 13.40, 9.89, -9.49));
        s = Shapes.or(s, Block.box(12.00, 12.51, -11.77, 13.40, 13.58, -11.02));
        s = Shapes.or(s, Block.box(12.00, 7.81, -10.04, 13.40, 8.89, -9.29));
        s = Shapes.or(s, Block.box(12.00, 10.66, -11.01, 13.40, 11.73, -10.25));
        return s;
    }

    /** {@code bed_plate1_pillow_small_ps4.geo.json}；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallPs4Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(21.07, 7.40, -6.71, 26.73, 7.50, -1.05));
        s = Shapes.or(s, Block.box(21.78, 7.00, -6.00, 26.02, 8.00, -1.76));
        return s;
    }

    /** {@code bed_plate1_pillow_small_ps5.geo.json}；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallPs5Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(4.25, 7.40, -0.42, 9.47, 7.50, 4.80));
        s = Shapes.or(s, Block.box(4.90, 7.00, 0.23, 8.82, 8.00, 4.15));
        return s;
    }

    /** {@code bed_plate1_pillow_small_ps4_cover.geo.json}；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallPs4CoverRaw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(21.07, 12.90, -6.71, 26.73, 13.00, -1.05));
        s = Shapes.or(s, Block.box(21.78, 12.50, -6.00, 26.02, 13.50, -1.76));
        return s;
    }

    /** {@code bed_plate1_pillow_small_ps5_cover.geo.json}；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallPs5CoverRaw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(4.25, 11.10, -0.42, 9.47, 11.20, 4.80));
        s = Shapes.or(s, Block.box(4.90, 10.70, 0.23, 8.82, 11.70, 4.15));
        return s;
    }
}
