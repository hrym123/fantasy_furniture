package org.lanye.fantasy_furniture.block.decor;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.block.FurnitureBlock;
import org.lanye.fantasy_furniture.block.entity.GreenSofaBlockEntity;
import org.lanye.fantasy_furniture.block.state.SofaPart;

/**
 * 绿色沙发：占地横向三格（左 / 中 / 右），仅中间格含 {@link GreenSofaBlockEntity} 与 GeckoLib 模型。
 * 水平 {@link org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock#FACING} 与放置逻辑见
 * {@link org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock}（{@link #getStateForPlacement} 覆盖以摆三联）。
 */
public class GreenSofaBlock extends FurnitureBlock {

    public static final EnumProperty<SofaPart> PART = EnumProperty.create("part", SofaPart.class);

    private static final VoxelShape SHAPE = Shapes.block();

    /** 拆除三联中一格时避免连锁 {@link #onRemove} 重复拆 sibling。 */
    private static final ThreadLocal<Boolean> SUPPRESS_SIBLING_BREAK = ThreadLocal.withInitial(() -> false);

    public GreenSofaBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, SofaPart.CENTER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 与烤箱、卡座等一致：朝向玩家（模型正面朝向放置者视线反方向）。
        Direction facing = context.getHorizontalDirection().getOpposite();
        // BlockPlaceContext#getClickedPos() 已是目标放置格（含替换可替换方块 / 贴在相邻面），勿再 relative(clickedFace)。
        BlockPos center = context.getClickedPos();
        Direction toLeft = facing.getCounterClockWise();
        Direction toRight = facing.getClockWise();
        BlockPos leftPos = center.relative(toLeft);
        BlockPos rightPos = center.relative(toRight);
        Level level = context.getLevel();
        var border = level.getWorldBorder();
        if (!border.isWithinBounds(center) || !border.isWithinBounds(leftPos) || !border.isWithinBounds(rightPos)) {
            return null;
        }
        if (!level.getBlockState(center).canBeReplaced(context)) {
            return null;
        }
        if (!level.getBlockState(leftPos).canBeReplaced(context)) {
            return null;
        }
        if (!level.getBlockState(rightPos).canBeReplaced(context)) {
            return null;
        }
        BlockPos below = center.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, SofaPart.CENTER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (state.getValue(PART) != SofaPart.CENTER) {
            return;
        }
        Direction facing = state.getValue(FACING);
        Direction toLeft = facing.getCounterClockWise();
        Direction toRight = facing.getClockWise();
        BlockPos leftPos = pos.relative(toLeft);
        BlockPos rightPos = pos.relative(toRight);
        level.setBlock(leftPos, state.setValue(PART, SofaPart.LEFT), Block.UPDATE_ALL);
        level.setBlock(rightPos, state.setValue(PART, SofaPart.RIGHT), Block.UPDATE_ALL);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        SofaPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        Direction toLeft = facing.getCounterClockWise();
        Direction toRight = facing.getClockWise();
        return switch (part) {
            case CENTER -> {
                BlockPos below = pos.below();
                yield level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
            }
            case LEFT -> neighborMatches(level, pos.relative(toRight), state, SofaPart.CENTER);
            case RIGHT -> neighborMatches(level, pos.relative(toLeft), state, SofaPart.CENTER);
        };
    }

    private static boolean neighborMatches(LevelReader level, BlockPos neighborPos, BlockState state, SofaPart expectedPart) {
        BlockState neighbor = level.getBlockState(neighborPos);
        return neighbor.is(state.getBlock())
                && neighbor.getValue(FACING) == state.getValue(FACING)
                && neighbor.getValue(PART) == expectedPart;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!SUPPRESS_SIBLING_BREAK.get()) {
                SUPPRESS_SIBLING_BREAK.set(true);
                try {
                    destroySiblingsExcept(level, pos, state);
                } finally {
                    SUPPRESS_SIBLING_BREAK.set(false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            if (!SUPPRESS_SIBLING_BREAK.get()) {
                SUPPRESS_SIBLING_BREAK.set(true);
                try {
                    destroySiblingsExcept(level, pos, state);
                } finally {
                    SUPPRESS_SIBLING_BREAK.set(false);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /** 拆掉除 {@code pos} 外的另两格，不掉落；被拆格的 {@link #onRemove} 内会因 flag 跳过。 */
    private static void destroySiblingsExcept(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction toLeft = facing.getCounterClockWise();
        Direction toRight = facing.getClockWise();
        SofaPart part = state.getValue(PART);
        BlockPos center =
                switch (part) {
                    case CENTER -> pos;
                    case LEFT -> pos.relative(toRight);
                    case RIGHT -> pos.relative(toLeft);
                };
        BlockPos leftPos = center.relative(toLeft);
        BlockPos rightPos = center.relative(toRight);
        for (BlockPos p : new BlockPos[] {leftPos, center, rightPos}) {
            if (p.equals(pos)) {
                continue;
            }
            BlockState s = level.getBlockState(p);
            if (s.is(state.getBlock())) {
                level.destroyBlock(p, false);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == SofaPart.CENTER ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) != SofaPart.CENTER) {
            return null;
        }
        return new GreenSofaBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
