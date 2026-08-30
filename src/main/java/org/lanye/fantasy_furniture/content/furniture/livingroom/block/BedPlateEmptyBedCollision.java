package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 1×2 床板空床体：FOOT/HEAD 北向外接盒（由各型号 geo + {@code --gecko-block} 裁切，大概轮廓）。
 *
 * <p>朝向用 {@link VoxelShapeRotation#rotateYFromNorthLikeGeckoBlockRenderer}（对齐画面，不用 bed_plate6
 * 选取用的 z.flip）。
 */
final class BedPlateEmptyBedCollision {

    private BedPlateEmptyBedCollision() {}

    /** 床板2型：两侧高栏，两格外接近整格。 */
    static final VoxelShape PLATE2_FOOT = Block.box(0.00, 0.00, 0.00, 16.00, 16.00, 16.00);

    static final VoxelShape PLATE2_HEAD = Block.box(0.00, 0.00, 0.00, 16.00, 16.00, 16.00);

    /** 床板3型。 */
    static final VoxelShape PLATE3_FOOT = Block.box(0.00, 0.00, 0.00, 16.00, 13.00, 16.00);

    static final VoxelShape PLATE3_HEAD = Block.box(0.00, 0.00, 0.00, 16.00, 15.50, 16.00);

    /** 床板4型：床尾偏矮垫、床头有板。 */
    static final VoxelShape PLATE4_FOOT = Block.box(0.50, 0.00, 0.00, 15.50, 3.00, 15.50);

    static final VoxelShape PLATE4_HEAD = Block.box(0.00, 0.00, 0.00, 16.00, 13.00, 16.00);

    /** 床板6型空床体（寝具选取形另叠）。 */
    static final VoxelShape PLATE6_FOOT = Block.box(0.00, 0.00, 0.00, 16.00, 5.00, 16.00);

    static final VoxelShape PLATE6_HEAD = Block.box(0.00, 0.00, 0.00, 16.00, 16.00, 16.00);

    static VoxelShape shapeFor(BlockState state, VoxelShape footNorth, VoxelShape headNorth) {
        VoxelShape north =
                state.getValue(BedBlock.PART) == BedPart.FOOT ? footNorth : headNorth;
        Direction facing = state.getValue(BedBlock.FACING);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }
}
