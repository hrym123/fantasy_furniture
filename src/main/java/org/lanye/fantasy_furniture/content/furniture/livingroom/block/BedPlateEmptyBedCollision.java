package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 1×2 床板空床体：FOOT/HEAD 北向多盒并集（按格裁切 geo，保留镂空；勿用整格外接盒）。
 *
 * <p>板2/3/4：模型在床尾局部 z∈[−16,16]（床头为负 z）。板6：manifest z.flip 后 z∈[0,32]，分格后再做局部
 * Z 镜像，使床尾板在格南、床头板在格北（对齐画面与选取 +180°Y 约定）。
 *
 * <p>朝向用 {@link VoxelShapeRotation#rotateYFromNorthLikeGeckoBlockRenderer}（对齐画面，不用选取链的
 * z.flip 旋转表）。
 */
final class BedPlateEmptyBedCollision {

    private BedPlateEmptyBedCollision() {}

    /** PLATE2_FOOT：geo bed_plate2 · clip z[0,16] · --gecko-block X；15 cubes */
    static final VoxelShape PLATE2_FOOT =
            Shapes.or(
                    Block.box(1.00, 1.00, 1.00, 2.00, 4.00, 15.00),
                    Block.box(15.00, 1.50, 6.00, 16.00, 2.50, 10.00),
                    Block.box(15.00, 3.50, 2.00, 16.00, 4.00, 14.00),
                    Block.box(2.00, 1.50, 14.00, 16.00, 4.00, 15.00),
                    Block.box(2.00, 1.50, 1.00, 16.00, 4.00, 2.00),
                    Block.box(2.00, 1.00, 1.00, 16.00, 1.50, 15.00),
                    Block.box(15.00, 1.50, 2.00, 16.00, 3.50, 6.00),
                    Block.box(15.00, 1.50, 10.00, 16.00, 3.50, 14.00),
                    Block.box(0.00, 5.00, 0.00, 1.00, 16.00, 15.00),
                    Block.box(0.00, 5.00, 15.00, 16.00, 16.00, 16.00),
                    Block.box(1.00, 4.00, 0.00, 16.00, 5.00, 16.00),
                    Block.box(1.00, 1.00, 0.00, 16.00, 4.00, 1.00),
                    Block.box(1.00, 1.00, 15.00, 16.00, 4.00, 16.00),
                    Block.box(1.00, 0.00, 0.00, 16.00, 1.00, 16.00),
                    Block.box(0.00, 0.00, 0.00, 1.00, 5.00, 16.00));

    /** PLATE2_HEAD：geo bed_plate2 · clip z[-16,0] · shift −16 · --gecko-block X；15 cubes */
    static final VoxelShape PLATE2_HEAD =
            Shapes.or(
                    Block.box(1.00, 1.00, 1.00, 2.00, 4.00, 15.00),
                    Block.box(15.00, 1.50, 6.00, 16.00, 2.50, 10.00),
                    Block.box(15.00, 3.50, 2.00, 16.00, 4.00, 14.00),
                    Block.box(2.00, 1.50, 14.00, 16.00, 4.00, 15.00),
                    Block.box(2.00, 1.50, 1.00, 16.00, 4.00, 2.00),
                    Block.box(2.00, 1.00, 1.00, 16.00, 1.50, 15.00),
                    Block.box(15.00, 1.50, 2.00, 16.00, 3.50, 6.00),
                    Block.box(15.00, 1.50, 10.00, 16.00, 3.50, 14.00),
                    Block.box(0.00, 5.00, 1.00, 1.00, 16.00, 16.00),
                    Block.box(0.00, 5.00, 0.00, 16.00, 16.00, 1.00),
                    Block.box(1.00, 4.00, 0.00, 16.00, 5.00, 16.00),
                    Block.box(1.00, 1.00, 0.00, 16.00, 4.00, 1.00),
                    Block.box(1.00, 1.00, 15.00, 16.00, 4.00, 16.00),
                    Block.box(1.00, 0.00, 0.00, 16.00, 1.00, 16.00),
                    Block.box(0.00, 0.00, 0.00, 1.00, 5.00, 16.00));

    /** PLATE3_FOOT：geo bed_plate3 · clip z[0,16] · --gecko-block X；15 cubes */
    static final VoxelShape PLATE3_FOOT =
            Shapes.or(
                    Block.box(0.50, 1.00, 0.00, 15.50, 3.00, 14.00),
                    Block.box(0.00, 0.00, 14.00, 2.00, 10.00, 16.00),
                    Block.box(0.10, 11.30, 14.10, 1.90, 12.70, 15.90),
                    Block.box(0.40, 10.00, 14.40, 1.60, 11.00, 15.60),
                    Block.box(0.20, 11.00, 14.20, 1.80, 13.00, 15.80),
                    Block.box(0.00, 11.60, 14.00, 2.00, 12.40, 16.00),
                    Block.box(14.00, 0.00, 14.00, 16.00, 10.00, 16.00),
                    Block.box(14.20, 11.00, 14.20, 15.80, 13.00, 15.80),
                    Block.box(14.00, 11.60, 14.00, 16.00, 12.40, 16.00),
                    Block.box(14.10, 11.30, 14.10, 15.90, 12.70, 15.90),
                    Block.box(14.40, 10.00, 14.40, 15.60, 11.00, 15.60),
                    Block.box(2.00, 1.00, 14.20, 14.00, 9.00, 15.80),
                    Block.box(10.77, 5.17, 14.20, 14.02, 8.42, 15.80),
                    Block.box(2.40, 5.60, 14.20, 5.23, 8.42, 15.80),
                    Block.box(3.80, 9.00, 14.20, 12.20, 10.80, 15.80));

    /** PLATE3_HEAD：geo bed_plate3 · clip z[-16,0] · shift −16 · --gecko-block X；15 cubes */
    static final VoxelShape PLATE3_HEAD =
            Shapes.or(
                    Block.box(0.50, 1.00, 2.00, 15.50, 3.00, 16.00),
                    Block.box(4.12, 10.92, 0.00, 12.12, 13.12, 2.00),
                    Block.box(12.83, 11.00, 0.00, 16.00, 14.54, 2.00),
                    Block.box(0.00, 11.00, 0.00, 3.41, 14.54, 2.00),
                    Block.box(2.00, 1.00, 0.00, 14.00, 11.00, 2.00),
                    Block.box(0.00, 0.00, 0.00, 2.00, 13.00, 2.00),
                    Block.box(14.00, 0.00, 0.00, 16.00, 13.00, 2.00),
                    Block.box(0.20, 13.50, 0.20, 1.80, 15.50, 1.80),
                    Block.box(0.00, 14.10, 0.00, 2.00, 14.90, 2.00),
                    Block.box(0.10, 13.80, 0.10, 1.90, 15.20, 1.90),
                    Block.box(0.40, 12.50, 0.40, 1.60, 13.50, 1.60),
                    Block.box(14.20, 13.50, 0.20, 15.80, 15.50, 1.80),
                    Block.box(14.00, 14.10, 0.00, 16.00, 14.90, 2.00),
                    Block.box(14.10, 13.80, 0.10, 15.90, 15.20, 1.90),
                    Block.box(14.40, 12.50, 0.40, 15.60, 13.50, 1.60));

    /** PLATE4_FOOT：geo bed_plate4 · clip z[0,16] · --gecko-block X；3 cubes */
    static final VoxelShape PLATE4_FOOT =
            Shapes.or(
                    Block.box(0.50, 2.00, 0.00, 15.50, 3.00, 15.50),
                    Block.box(0.50, 0.00, 14.50, 1.50, 2.00, 15.50),
                    Block.box(14.50, 0.00, 14.50, 15.50, 2.00, 15.50));

    /** PLATE4_HEAD：geo bed_plate4 · clip z[-16,0] · shift −16 · --gecko-block X；11 cubes */
    static final VoxelShape PLATE4_HEAD =
            Shapes.or(
                    Block.box(1.00, 2.00, 0.00, 15.00, 7.00, 1.00),
                    Block.box(1.00, 12.00, 0.00, 15.00, 13.00, 1.00),
                    Block.box(0.50, 2.00, 1.00, 15.50, 3.00, 16.00),
                    Block.box(2.00, 7.00, 0.00, 3.00, 12.00, 1.00),
                    Block.box(13.00, 7.00, 0.00, 14.00, 12.00, 1.00),
                    Block.box(11.00, 7.00, 0.00, 12.00, 12.00, 1.00),
                    Block.box(4.00, 7.00, 0.00, 5.00, 12.00, 1.00),
                    Block.box(9.00, 7.00, 0.00, 10.00, 12.00, 1.00),
                    Block.box(6.00, 7.00, 0.00, 7.00, 12.00, 1.00),
                    Block.box(15.00, 0.00, 0.00, 16.00, 13.00, 1.00),
                    Block.box(0.00, 0.00, 0.00, 1.00, 13.00, 1.00));

    /**
     * PLATE6_FOOT：geo bed_plate6 · clip z[0,16] · --gecko-block X · 局部 Z 镜像；4 cubes（对齐选取
     * +180°Y 后床尾格）。
     */
    static final VoxelShape PLATE6_FOOT =
            Shapes.or(
                    Block.box(0.00, 3.00, 0.00, 16.00, 5.00, 15.00),
                    Block.box(0.00, 0.00, 15.00, 16.00, 5.00, 16.00),
                    Block.box(15.00, 1.00, 0.00, 16.00, 2.00, 15.00),
                    Block.box(0.00, 1.00, 0.00, 1.00, 2.00, 15.00));

    /**
     * PLATE6_HEAD：geo bed_plate6 · clip z[16,32] · shift +16 · --gecko-block X · 局部 Z 镜像；5
     * cubes（床头板在格北，勿整格填实）。
     */
    static final VoxelShape PLATE6_HEAD =
            Shapes.or(
                    Block.box(0.00, 3.00, 1.00, 16.00, 5.00, 16.00),
                    Block.box(0.00, 15.00, 0.00, 16.00, 16.00, 1.30),
                    Block.box(0.00, 0.00, 0.00, 16.00, 15.00, 1.00),
                    Block.box(15.00, 1.00, 1.00, 16.00, 2.00, 16.00),
                    Block.box(0.00, 1.00, 1.00, 1.00, 2.00, 16.00));

    static VoxelShape shapeFor(BlockState state, VoxelShape footNorth, VoxelShape headNorth) {
        VoxelShape north =
                state.getValue(BedBlock.PART) == BedPart.FOOT ? footNorth : headNorth;
        Direction facing = state.getValue(BedBlock.FACING);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }
}
