package org.lanye.fantasy_furniture.block.facing;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.lanye.fantasy_furniture.block.state.BanquetteShape;

/**
 * 卡座 {@link BanquetteShape} 拐角判定：仅依赖 {@link BlockGetter} 与「邻格是否为可衔接座席」谓词。
 * <p>
 * 生产环境由 {@link BanquetteBlock} 传入 {@code state -> state.getBlock() instanceof BanquetteBlock}。单元测试勿依赖
 * {@link net.minecraft.world.level.block.Blocks}（未引导注册表时 {@code defaultBlockState()} 会失败），应对
 * {@link BlockState} 打桩 {@link BlockStateProperties#HORIZONTAL_FACING} 并用谓词区分「邻格座席」实例。
 */
public final class BanquetteShapeRules {

    private BanquetteShapeRules() {}

    public static BanquetteShape computeShape(
            BlockState state, BlockGetter level, BlockPos pos, Predicate<BlockState> isSeat) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockState ahead = level.getBlockState(pos.relative(facing));
        if (!isSeat.test(ahead)) {
            return BanquetteShape.STRAIGHT;
        }
        Direction other = ahead.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (other == facing.getClockWise()) {
            if (shouldSuppressCornerLeft(level, pos, facing, isSeat)) {
                return BanquetteShape.STRAIGHT;
            }
            return BanquetteShape.CORNER_LEFT;
        }
        if (other == facing.getCounterClockWise()) {
            if (shouldSuppressCornerRight(level, pos, facing, isSeat)) {
                return BanquetteShape.STRAIGHT;
            }
            return BanquetteShape.CORNER_RIGHT;
        }
        return BanquetteShape.STRAIGHT;
    }

    private static boolean shouldSuppressCornerLeft(
            BlockGetter level, BlockPos pos, Direction facing, Predicate<BlockState> isSeat) {
        BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        if (!isSeat.test(left)) {
            return false;
        }
        Direction lf = left.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return lf == facing || lf == facing.getClockWise();
    }

    private static boolean shouldSuppressCornerRight(
            BlockGetter level, BlockPos pos, Direction facing, Predicate<BlockState> isSeat) {
        BlockState right = level.getBlockState(pos.relative(facing.getClockWise()));
        if (!isSeat.test(right)) {
            return false;
        }
        Direction rf = right.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return rf == facing || rf == facing.getCounterClockWise();
    }
}
