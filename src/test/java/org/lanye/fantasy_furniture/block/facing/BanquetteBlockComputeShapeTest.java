package org.lanye.fantasy_furniture.block.facing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.lanye.fantasy_furniture.block.state.BanquetteShape;
import org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock;

/**
 * {@link BanquetteBlock#computeShape} 仅依赖 {@link BlockGetter}，用 Map 桩代替 {@link net.minecraft.world.level.Level}。
 */
@ExtendWith(MockitoExtension.class)
class BanquetteBlockComputeShapeTest {

    private static BanquetteBlock banquette;

    @Mock
    private BlockGetter level;

    @BeforeAll
    static void createBlock() {
        banquette = new BanquetteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD));
    }

    @Test
    void straight_whenAheadNotBanquette() {
        BlockPos self = BlockPos.ZERO;
        BlockState state = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        stubMap(
                Map.of(
                        self, state,
                        self.relative(Direction.NORTH), Blocks.AIR.defaultBlockState()));
        assertEquals(BanquetteShape.STRAIGHT, BanquetteBlock.computeShape(state, level, self));
    }

    @Test
    void cornerLeft_whenAheadFacesClockwise() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        BlockState aheadState =
                banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.EAST);
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), aheadState));
        assertEquals(BanquetteShape.CORNER_LEFT, BanquetteBlock.computeShape(selfState, level, self));
    }

    @Test
    void cornerLeft_suppressed_whenLeftNeighborSameFacing() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        BlockState aheadState =
                banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.EAST);
        BlockPos left = self.relative(Direction.WEST);
        BlockState leftState = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        Map<BlockPos, BlockState> map = new HashMap<>();
        map.put(self, selfState);
        map.put(self.relative(Direction.NORTH), aheadState);
        map.put(left, leftState);
        stubMap(map);
        assertEquals(BanquetteShape.STRAIGHT, BanquetteBlock.computeShape(selfState, level, self));
    }

    @Test
    void cornerRight_whenAheadFacesCounterClockwise() {
        BlockPos self = new BlockPos(3, 0, 3);
        BlockState selfState = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        BlockState aheadState =
                banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.WEST);
        stubMap(
                Map.of(
                        self, selfState,
                        self.relative(Direction.NORTH), aheadState));
        assertEquals(BanquetteShape.CORNER_RIGHT, BanquetteBlock.computeShape(selfState, level, self));
    }

    @Test
    void cornerRight_suppressed_whenRightNeighborSameFacing() {
        BlockPos self = BlockPos.ZERO;
        BlockState selfState = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        BlockState aheadState =
                banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.WEST);
        BlockPos right = self.relative(Direction.EAST);
        BlockState rightState = banquette.defaultBlockState().setValue(GeolibFacingEntityBlock.FACING, Direction.NORTH);
        Map<BlockPos, BlockState> map = new HashMap<>();
        map.put(self, selfState);
        map.put(self.relative(Direction.NORTH), aheadState);
        map.put(right, rightState);
        stubMap(map);
        assertEquals(BanquetteShape.STRAIGHT, BanquetteBlock.computeShape(selfState, level, self));
    }

    private void stubMap(Map<BlockPos, BlockState> map) {
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(invocation -> map.getOrDefault(invocation.getArgument(0), Blocks.AIR.defaultBlockState()));
    }
}
