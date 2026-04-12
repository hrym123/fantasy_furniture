package org.lanye.fantasy_furniture.block.facing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.lanye.fantasy_furniture.block.state.BanquetteShape;
import org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock;

/**
 * 单元测试 {@link BanquetteShapeRules#computeShape}：用 {@link Blocks#FURNACE} 模拟「可衔接座席」（与卡座共用水平
 * {@link GeolibFacingEntityBlock#FACING}），避免在 JUnit 中 {@code new BanquetteBlock} 或调用
 * {@link net.minecraft.server.Bootstrap#bootStrap()}（Forge 下会触发网络层初始化失败）。
 * <p>
 * 仅依赖 {@link BlockGetter#getBlockState}，用 {@link Map} + Mockito 桩代替真实 {@link net.minecraft.world.level.Level}。
 */
@ExtendWith(MockitoExtension.class)
class BanquetteBlockComputeShapeTest {

    /** 测试中把熔炉当作「邻格卡座」；与生产环境 {@code instanceof BanquetteBlock} 对应。 */
    private static final Predicate<BlockState> SEAT_LIKE = s -> s.is(Blocks.FURNACE);

    @Mock
    private BlockGetter level;

    private static BlockState furnaceFacing(Direction d) {
        return Blocks.FURNACE.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, d);
    }

    @Test
    void straight_whenAheadNotBanquette() {
        BlockPos self = BlockPos.ZERO;
        BlockState state = furnaceFacing(Direction.NORTH);
        stubMap(
                Map.of(
                        self, state,
                        self.relative(Direction.NORTH), Blocks.AIR.defaultBlockState()));
        assertEquals(BanquetteShape.STRAIGHT, BanquetteShapeRules.computeShape(state, level, self, SEAT_LIKE));
    }

    @Test
    void cornerLeft_whenAheadFacesClockwise() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = furnaceFacing(Direction.NORTH);
        BlockState aheadState = furnaceFacing(Direction.EAST);
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), aheadState));
        assertEquals(BanquetteShape.CORNER_LEFT, BanquetteShapeRules.computeShape(selfState, level, self, SEAT_LIKE));
    }

    @Test
    void cornerLeft_suppressed_whenLeftNeighborSameFacing() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = furnaceFacing(Direction.NORTH);
        BlockState aheadState = furnaceFacing(Direction.EAST);
        BlockPos left = self.relative(Direction.WEST);
        BlockState leftState = furnaceFacing(Direction.NORTH);
        Map<BlockPos, BlockState> map = new HashMap<>();
        map.put(self, selfState);
        map.put(self.relative(Direction.NORTH), aheadState);
        map.put(left, leftState);
        stubMap(map);
        assertEquals(BanquetteShape.STRAIGHT, BanquetteShapeRules.computeShape(selfState, level, self, SEAT_LIKE));
    }

    @Test
    void cornerRight_whenAheadFacesCounterClockwise() {
        BlockPos self = new BlockPos(3, 0, 3);
        BlockState selfState = furnaceFacing(Direction.NORTH);
        BlockState aheadState = furnaceFacing(Direction.WEST);
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), aheadState));
        assertEquals(BanquetteShape.CORNER_RIGHT, BanquetteShapeRules.computeShape(selfState, level, self, SEAT_LIKE));
    }

    @Test
    void cornerRight_suppressed_whenRightNeighborSameFacing() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = furnaceFacing(Direction.NORTH);
        BlockState aheadState = furnaceFacing(Direction.WEST);
        BlockPos right = self.relative(Direction.EAST);
        BlockState rightState = furnaceFacing(Direction.NORTH);
        Map<BlockPos, BlockState> map = new HashMap<>();
        map.put(self, selfState);
        map.put(self.relative(Direction.NORTH), aheadState);
        map.put(right, rightState);
        stubMap(map);
        assertEquals(BanquetteShape.STRAIGHT, BanquetteShapeRules.computeShape(selfState, level, self, SEAT_LIKE));
    }

    private void stubMap(Map<BlockPos, BlockState> map) {
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(invocation -> map.getOrDefault(invocation.getArgument(0), Blocks.AIR.defaultBlockState()));
    }
}
