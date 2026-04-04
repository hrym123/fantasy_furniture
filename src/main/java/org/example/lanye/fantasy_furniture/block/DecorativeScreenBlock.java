package org.example.lanye.fantasy_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 屏风：游戏逻辑上为<strong>两格高</strong>（下半块 + 上半块）、带朝向，与门在占用空间上类似；右键不交互。
 * <p>
 * <strong>模型与贴图</strong>由 {@code assets/.../models/block/}、{@code textures/} 等资源配置，不在此类中维护；
 * 后续请勿在无关重构中改动屏风模型文件；表现形状以你提供的资源为准。
 */
public class DecorativeScreenBlock extends HorizontalDirectionalBlock {

    public static final net.minecraft.world.level.block.state.properties.EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 0, 0, 3, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(13, 0, 0, 16, 16, 16);

    public DecorativeScreenBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            return defaultBlockState().setValue(FACING, facing).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            if (direction == Direction.DOWN && !neighborState.is(this)) {
                return Blocks.AIR.defaultBlockState();
            }
        } else {
            if (direction == Direction.UP && !neighborState.is(this)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                BlockPos below = pos.below();
                if (level.getBlockState(below).is(this)) {
                    if (!player.isCreative()) {
                        Block.popResource(level, pos, new ItemStack(this.asItem()));
                    }
                    level.setBlock(below, Blocks.AIR.defaultBlockState(), 35);
                }
            } else {
                BlockPos above = pos.above();
                if (level.getBlockState(above).is(this)) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Forge {@code ServerPlayerGameMode#destroyBlock} 在创造模式下仍会调用此方法；若调用 {@code super.destroy}，
     * 会通过 {@code Block.dropResources}（无玩家实体）仍执行战利品表，导致下半格在创造模式异常掉落。
     * 生存破坏的掉落由 {@code playerDestroy} 负责；本方块无方块实体，无需执行 {@code super.destroy} 中其余逻辑。
     */
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
    }

    /**
     * 第二道防线：部分环境下仍可能通过 {@link Block#dropResources} 调用战利品；
     * 创造玩家、以及无实体且空手上下文下的下半格（对应 {@link #destroy} 路径）不产出掉落。
     * 爆炸破坏保留战利品：存在 {@link LootContextParams#EXPLOSION_RADIUS} 时不拦截。
     */
    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        LootParams params = builder.create(LootContextParamSets.BLOCK);
        // 1.20.1：getOptionalParameter 返回可空 T，不是 java.util.Optional
        Entity breaker = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (breaker instanceof Player p && p.isCreative()) {
            return List.of();
        }
        Float explosionRadius = params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER
                && breaker == null
                && params.getParameter(LootContextParams.TOOL).isEmpty()
                && explosionRadius == null) {
            return List.of();
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (player.isCreative()) {
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> Shapes.block();
        };
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }
}
