package org.lanye.fantasy_furniture.geolib;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * 带水平朝向的 GeckoLib 方块实体基类：与熔炉、装饰屏风等一致，放置时 {@link #FACING} 指向玩家
 * （{@link BlockPlaceContext#getHorizontalDirection()}{@code .getOpposite()}）。
 * <p>
 * 仅含 {@code facing} 的子类无需再实现 {@link #getStateForPlacement} / {@link #rotate} / {@link #mirror}；
 * 若另有状态属性，须在 {@link #createBlockStateDefinition} 中先 {@code super} 再 {@code builder.add(...)}，
 * 并在子类构造里 {@link #registerDefaultState} 写全量默认值。
 */
public abstract class GeolibFacingEntityBlock extends GeolibEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected GeolibFacingEntityBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
