package org.lanye.fantasy_furniture.content.furniture.livingroom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/** 床板系：床尾格坐标（方块实体数据锚点）。 */
public final class BedPlateBedFootPos {

    private BedPlateBedFootPos() {}

    public static BlockPos footPos(BlockState state, BlockPos anyPartPos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return anyPartPos;
        }
        return anyPartPos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }
}
