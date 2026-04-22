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
        boolean aheadIsSeat = isSeat.test(ahead);
        Direction aheadFacing = aheadIsSeat ? ahead.getValue(BlockStateProperties.HORIZONTAL_FACING) : null;

        BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        boolean leftIsSeat = isSeat.test(left);
        Direction leftFacing = leftIsSeat ? left.getValue(BlockStateProperties.HORIZONTAL_FACING) : null;

        BlockState right = level.getBlockState(pos.relative(facing.getClockWise()));
        boolean rightIsSeat = isSeat.test(right);
        Direction rightFacing = rightIsSeat ? right.getValue(BlockStateProperties.HORIZONTAL_FACING) : null;

        return computeShapeFromNeighborhood(
                facing, aheadIsSeat, aheadFacing, leftIsSeat, leftFacing, rightIsSeat, rightFacing);
    }

    /**
     * 纯逻辑：由当前朝向与相邻三格（前/左/右）的「是否为座席 + 座席朝向」推导形态。
     * <p>
     * 包级可见，供无需 Minecraft 运行时初始化的 JUnit 直接覆盖规则分支。
     */
    static BanquetteShape computeShapeFromNeighborhood(
            Direction facing,
            boolean aheadIsSeat,
            Direction aheadFacing,
            boolean leftIsSeat,
            Direction leftFacing,
            boolean rightIsSeat,
            Direction rightFacing) {
        if (!aheadIsSeat) {
            return BanquetteShape.STRAIGHT;
        }
        if (aheadFacing == facing.getClockWise()) {
            boolean suppress = leftIsSeat && (leftFacing == facing || leftFacing == facing.getClockWise());
            return suppress ? BanquetteShape.STRAIGHT : BanquetteShape.CORNER_LEFT;
        }
        if (aheadFacing == facing.getCounterClockWise()) {
            boolean suppress = rightIsSeat && (rightFacing == facing || rightFacing == facing.getCounterClockWise());
            return suppress ? BanquetteShape.STRAIGHT : BanquetteShape.CORNER_RIGHT;
        }
        return BanquetteShape.STRAIGHT;
    }
}
