package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 床品/枕头在「北向基准 geo」下的选取用 {@link VoxelShape}（与 {@link BedPlate6Block#getShape} 合并）。
 *
 * <p>数值由 {@code tools/bed6/bed_plate6_voxel_pick_from_geo.py} 从对应 geo 生成，裁切盒为 {@code [0,16]×[0,16]×[0,32]}（床尾方块局部、含床头半段 z），
 * 再经 {@link org.lanye.reverie_core.util.VoxelShapeRotation} 与 {@link org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer} 同款水平朝向额外 180° Y 对齐到方块状态。
 *
 * @see org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer#rotateBlock
 */
public final class BedPlate6PickShapesNorth {

    private BedPlate6PickShapesNorth() {}

    /** 有床单时并入的北向选取体（与被套/枕头并集）。 */
    public static VoxelShape unionNorthForPick(BedPlate6BlockEntity plate) {
        if (!plate.hasDuvet()) {
            return Shapes.empty();
        }
        VoxelShape s = duvetNorth();
        if (plate.hasCover()) {
            s = Shapes.or(s, duvetCoverNorth());
        }
        if (plate.hasLargePillow()) {
            s = Shapes.or(s, largePillowNorth(plate.getLargePillowStyleId()));
        }
        int n = plate.getMediumPillowCount();
        boolean large = plate.hasLargePillow();
        if (n == 2) {
            s = Shapes.or(s, pillowMediumPairRearNorth());
            s = Shapes.or(s, pillowMediumPairFrontNorth());
        } else if (n == 1 && large) {
            s = Shapes.or(s, pillowMediumPairFrontNorth());
        } else if (n == 1) {
            s = Shapes.or(s, pillowMediumSoloNorth());
        }
        if (plate.hasSmallPillow()) {
            s = Shapes.or(s, pillowSmallStackNorth());
        }
        return s;
    }

    private static VoxelShape largePillowNorth(int styleId) {
        if (!BedPlate6LargePillowStyles.isValid(styleId)) {
            return Shapes.empty();
        }
        return switch (styleId) {
            case 1 -> pillowLargeStripedNorth();
            case 2 -> pillowLargePlainNorth();
            case 3 -> pillowLargePlaidNorth();
            default -> Shapes.empty();
        };
    }

    private static VoxelShape duvetNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(0.0, 5.0, 1.0, 16.0, 7.0, 31.0));
        return s;
    }

    private static VoxelShape duvetCoverNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(1.0, 6.5, 18.0, 15.0, 9.5, 24.0));
        s = Shapes.or(s, Block.box(0.5, 6.5, 0.0, 15.5, 8.5, 18.0));
        s = Shapes.or(s, Block.box(0.0, 3.5, 0.0, 16.0, 6.5, 24.0));
        s = Shapes.or(s, Block.box(14.5, 6.5, 0.0, 16.0, 9.0, 18.0));
        s = Shapes.or(s, Block.box(14.5, 6.5, 18.0, 16.0, 10.0, 24.0));
        s = Shapes.or(s, Block.box(0.0, 6.5, 0.0, 1.5, 9.0, 18.0));
        s = Shapes.or(s, Block.box(0.0, 6.5, 18.0, 1.5, 10.0, 24.0));
        return s;
    }

    private static VoxelShape pillowLargeStripedNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
        s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
        return s;
    }

    private static VoxelShape pillowLargePlainNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
        s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
        return s;
    }

    private static VoxelShape pillowLargePlaidNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
        s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
        return s;
    }

    private static VoxelShape pillowMediumSoloNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(3.0, 7.0, 24.0, 13.0, 9.0, 31.0));
        return s;
    }

    private static VoxelShape pillowMediumPairFrontNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(1.0, 5.5, 24.5, 11.0, 12.5, 29.0));
        return s;
    }

    private static VoxelShape pillowMediumPairRearNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(5.0, 7.0, 28.5, 15.0, 14.0, 30.5));
        return s;
    }

    private static VoxelShape pillowSmallStackNorth() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(11.5, 7.5, 26.0, 15.5, 11.5, 27.5));
        s = Shapes.or(s, Block.box(12.0, 8.0, 26.0, 15.0, 11.0, 28.0));
        return s;
    }
}
