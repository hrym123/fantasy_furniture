package org.lanye.fantasy_furniture.block.facing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.lanye.fantasy_furniture.block.state.BanquetteShape;

/**
 * 单元测试 {@link BanquetteShapeRules#computeShape}。不访问 {@link net.minecraft.world.level.block.Blocks}
 *（JUnit classpath 下可能未执行原版注册表引导，{@code Blocks.*.defaultBlockState()} 会失败），仅用 Mockito 桩
 * {@link BlockState#getValue}，用 {@link Set}{@code ::contains} 模拟「邻格是否可衔接座席」谓词。
 */
@ExtendWith(MockitoExtension.class)
class BanquetteBlockComputeShapeTest {

    @Mock
    private BlockGetter level;

    /** 仅桩 {@link BlockStateProperties#HORIZONTAL_FACING}，供规则读取朝向。 */
    private static BlockState seatFacing(Direction d) {
        BlockState state = mock(BlockState.class);
        when(state.getValue(BlockStateProperties.HORIZONTAL_FACING)).thenReturn(d);
        return state;
    }

    private static BlockState nonSeat() {
        return mock(BlockState.class);
    }

    @Test
    void straight_whenAheadNotBanquette() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = seatFacing(Direction.NORTH);
        BlockState ahead = nonSeat();
        Predicate<BlockState> noNeighborIsSeat = s -> false;
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), ahead));
        assertEquals(BanquetteShape.STRAIGHT, BanquetteShapeRules.computeShape(selfState, level, self, noNeighborIsSeat));
    }

    @Test
    void cornerLeft_whenAheadFacesClockwise() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = seatFacing(Direction.NORTH);
        BlockState aheadState = seatFacing(Direction.EAST);
        Predicate<BlockState> isSeat = Set.of(aheadState)::contains;
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), aheadState));
        assertEquals(BanquetteShape.CORNER_LEFT, BanquetteShapeRules.computeShape(selfState, level, self, isSeat));
    }

    @Test
    void cornerLeft_suppressed_whenLeftNeighborSameFacing() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = seatFacing(Direction.NORTH);
        BlockState aheadState = seatFacing(Direction.EAST);
        BlockPos left = self.relative(Direction.WEST);
        BlockState leftState = seatFacing(Direction.NORTH);
        Predicate<BlockState> isSeat = Set.of(aheadState, leftState)::contains;
        Map<BlockPos, BlockState> map = new HashMap<>();
        map.put(self, selfState);
        map.put(self.relative(Direction.NORTH), aheadState);
        map.put(left, leftState);
        stubMap(map);
        assertEquals(BanquetteShape.STRAIGHT, BanquetteShapeRules.computeShape(selfState, level, self, isSeat));
    }

    @Test
    void cornerRight_whenAheadFacesCounterClockwise() {
        BlockPos self = new BlockPos(3, 0, 3);
        BlockState selfState = seatFacing(Direction.NORTH);
        BlockState aheadState = seatFacing(Direction.WEST);
        Predicate<BlockState> isSeat = Set.of(aheadState)::contains;
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), aheadState));
        assertEquals(BanquetteShape.CORNER_RIGHT, BanquetteShapeRules.computeShape(selfState, level, self, isSeat));
    }

    @Test
    void cornerRight_suppressed_whenRightNeighborSameFacing() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = seatFacing(Direction.NORTH);
        BlockState aheadState = seatFacing(Direction.WEST);
        BlockPos right = self.relative(Direction.EAST);
        BlockState rightState = seatFacing(Direction.NORTH);
        Predicate<BlockState> isSeat = Set.of(aheadState, rightState)::contains;
        Map<BlockPos, BlockState> map = new HashMap<>();
        map.put(self, selfState);
        map.put(self.relative(Direction.NORTH), aheadState);
        map.put(right, rightState);
        stubMap(map);
        assertEquals(BanquetteShape.STRAIGHT, BanquetteShapeRules.computeShape(selfState, level, self, isSeat));
    }

    /** 缺省位置为「非座席」占位状态，不加入任何 {@code isSeat} 集合即可。 */
    private void stubMap(Map<BlockPos, BlockState> map) {
        BlockState filler = nonSeat();
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(invocation -> map.getOrDefault(invocation.getArgument(0), filler));
    }
}
