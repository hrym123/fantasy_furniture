package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;

/**
 * 床板1型枕头选取：自组合导出 geo 的北向体素，再按默认 Geo 渲染做 X 镜像（相对床尾右锚点）。
 * 与床单裁切同一坐标系，不另加厚。
 */
public final class BedPlate1PillowPickShapes {

    private BedPlate1PillowPickShapes() {}

    private static final VoxelShape LARGE_S1 = mirrorGeckoX(largeS1Raw());
    private static final VoxelShape LARGE_S2 = mirrorGeckoX(largeS2Raw());
    private static final VoxelShape MEDIUM_P1 = mirrorGeckoX(mediumP1Raw());
    private static final VoxelShape MEDIUM_P2 = mirrorGeckoX(mediumP2Raw());
    private static final VoxelShape MEDIUM_S1 = mirrorGeckoX(mediumS1Raw());
    private static final VoxelShape MEDIUM_S2 = mirrorGeckoX(mediumS2Raw());
    private static final VoxelShape SMALL_X4 = mirrorGeckoX(smallX4Raw());
    private static final VoxelShape SMALL_X5 = mirrorGeckoX(smallX5Raw());
    private static final VoxelShape SMALL_X6 = mirrorGeckoX(smallX6Raw());
    private static final VoxelShape SMALL_X7 = mirrorGeckoX(smallX7Raw());

    public static VoxelShape northFor(BedPlateSheetPillowSlots slots, BedPlate1CollisionShapes.PickedLayer layer) {
        if (slots == null || layer == null) {
            return Shapes.empty();
        }
        return switch (layer) {
            case LARGE_1 -> slots.hasLargeSlot(1) ? LARGE_S1 : Shapes.empty();
            case LARGE_2 -> slots.hasLargeSlot(2) ? LARGE_S2 : Shapes.empty();
            case MEDIUM -> medium(slots);
            case SMALL -> small(slots);
            case BODY, DUVET, DUVET_COVER -> Shapes.empty();
        };
    }

    private static VoxelShape medium(BedPlateSheetPillowSlots slots) {
        if (!slots.hasMedium()) {
            return Shapes.empty();
        }
        int side = slots.mediumSide();
        boolean standing = slots.hasLargeSlot(side);
        if (side == 2) {
            return standing ? MEDIUM_S2 : MEDIUM_P2;
        }
        return standing ? MEDIUM_S1 : MEDIUM_P1;
    }

    private static VoxelShape small(BedPlateSheetPillowSlots slots) {
        if (!slots.hasSmall()) {
            return Shapes.empty();
        }
        return switch (slots.smallPlace()) {
            case 5 -> SMALL_X5;
            case 6 -> SMALL_X6;
            case 7 -> SMALL_X7;
            default -> SMALL_X4;
        };
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
        s = Shapes.or(s, Block.box(16.50, 6.96, -13.96, 29.50, 13.50, -11.09));
        s = Shapes.or(s, Block.box(17.50, 6.61, -14.79, 28.50, 13.84, -10.26));
        return s;
    }

    /** {@code bed_plate1_pillow_large_s2.geo.json} sha256[:12]=0b2d8e401587；尚未做 Gecko X 镜像。 */
    private static VoxelShape largeS2Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(0.50, 6.96, -13.96, 13.50, 13.50, -11.09));
        s = Shapes.or(s, Block.box(1.50, 6.61, -14.79, 12.50, 13.84, -10.26));
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
        s = Shapes.or(s, Block.box(19.00, 6.62, -9.92, 29.00, 13.85, -5.40));
        s = Shapes.or(s, Block.box(17.60, 6.89, -9.09, 19.00, 7.96, -8.34));
        s = Shapes.or(s, Block.box(17.60, 9.73, -8.13, 19.00, 10.81, -7.38));
        s = Shapes.or(s, Block.box(17.60, 11.58, -7.36, 19.00, 12.66, -6.61));
        s = Shapes.or(s, Block.box(17.60, 8.73, -8.33, 19.00, 9.81, -7.57));
        s = Shapes.or(s, Block.box(17.60, 12.43, -6.80, 19.00, 13.51, -6.04));
        s = Shapes.or(s, Block.box(17.60, 7.89, -8.89, 19.00, 8.96, -8.14));
        s = Shapes.or(s, Block.box(17.60, 10.58, -7.56, 19.00, 11.66, -6.81));
        s = Shapes.or(s, Block.box(29.00, 6.89, -9.09, 30.40, 7.96, -8.34));
        s = Shapes.or(s, Block.box(29.00, 9.73, -8.13, 30.40, 10.81, -7.38));
        s = Shapes.or(s, Block.box(29.00, 11.58, -7.36, 30.40, 12.66, -6.61));
        s = Shapes.or(s, Block.box(29.00, 8.73, -8.33, 30.40, 9.81, -7.57));
        s = Shapes.or(s, Block.box(29.00, 12.43, -6.80, 30.40, 13.51, -6.04));
        s = Shapes.or(s, Block.box(29.00, 7.89, -8.89, 30.40, 8.96, -8.14));
        s = Shapes.or(s, Block.box(29.00, 10.58, -7.56, 30.40, 11.66, -6.81));
        return s;
    }

    /** {@code bed_plate1_pillow_medium_s2.geo.json} sha256[:12]=b30a16576540；尚未做 Gecko X 镜像。 */
    private static VoxelShape mediumS2Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(2.00, 6.62, -9.92, 12.00, 13.85, -5.40));
        s = Shapes.or(s, Block.box(0.60, 6.89, -9.09, 2.00, 7.96, -8.34));
        s = Shapes.or(s, Block.box(0.60, 9.73, -8.13, 2.00, 10.81, -7.38));
        s = Shapes.or(s, Block.box(0.60, 11.58, -7.36, 2.00, 12.66, -6.61));
        s = Shapes.or(s, Block.box(0.60, 8.73, -8.33, 2.00, 9.81, -7.57));
        s = Shapes.or(s, Block.box(0.60, 12.43, -6.80, 2.00, 13.51, -6.04));
        s = Shapes.or(s, Block.box(0.60, 7.89, -8.89, 2.00, 8.96, -8.14));
        s = Shapes.or(s, Block.box(0.60, 10.58, -7.56, 2.00, 11.66, -6.81));
        s = Shapes.or(s, Block.box(12.00, 6.89, -9.09, 13.40, 7.96, -8.34));
        s = Shapes.or(s, Block.box(12.00, 9.73, -8.13, 13.40, 10.81, -7.38));
        s = Shapes.or(s, Block.box(12.00, 11.58, -7.36, 13.40, 12.66, -6.61));
        s = Shapes.or(s, Block.box(12.00, 8.73, -8.33, 13.40, 9.81, -7.57));
        s = Shapes.or(s, Block.box(12.00, 12.43, -6.80, 13.40, 13.51, -6.04));
        s = Shapes.or(s, Block.box(12.00, 7.89, -8.89, 13.40, 8.96, -8.14));
        s = Shapes.or(s, Block.box(12.00, 10.58, -7.56, 13.40, 11.66, -6.81));
        return s;
    }

    /** {@code bed_plate1_pillow_small_x4.geo.json} sha256[:12]=5b0bd7f5ccd5；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallX4Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(21.07, 7.40, -6.71, 26.73, 7.50, -1.05));
        s = Shapes.or(s, Block.box(21.78, 7.00, -6.00, 26.02, 8.00, -1.76));
        return s;
    }

    /** {@code bed_plate1_pillow_small_x5.geo.json} sha256[:12]=8a3f764e70bc；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallX5Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(4.25, 7.40, -0.42, 9.47, 7.50, 4.80));
        s = Shapes.or(s, Block.box(4.90, 7.00, 0.23, 8.82, 8.00, 4.15));
        return s;
    }

    /** {@code bed_plate1_pillow_small_x6.geo.json} sha256[:12]=6f5c2d0305fa；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallX6Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(21.07, 12.90, -6.71, 26.73, 13.00, -1.05));
        s = Shapes.or(s, Block.box(21.78, 12.50, -6.00, 26.02, 13.50, -1.76));
        return s;
    }

    /** {@code bed_plate1_pillow_small_x7.geo.json} sha256[:12]=3aee7a702f2d；尚未做 Gecko X 镜像。 */
    private static VoxelShape smallX7Raw() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(4.25, 11.10, -0.42, 9.47, 11.20, 4.80));
        s = Shapes.or(s, Block.box(4.90, 10.70, 0.23, 8.82, 11.70, 4.15));
        return s;
    }
}
