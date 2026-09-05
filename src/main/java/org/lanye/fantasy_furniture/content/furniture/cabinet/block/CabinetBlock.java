package org.lanye.fantasy_furniture.content.furniture.cabinet.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 柜子：四向放置；三层各可放入一件任意物品（展示框式）。
 *
 * <ul>
 *   <li>持物右击空槽 → 放入 1 件
 *   <li>空手右击有物槽 → 展品绕竖直轴 +45°（类似展示框）
 *   <li>潜行空手右击有物槽 → 取出
 *   <li>柜子1型：geo 高 48 → 竖向三格 {@link #PART}（0 底/BE+Geo，1/2 仅碰撞）；点哪格进哪层
 *   <li>柜子2型：单格，按命中高度映射层腔
 * </ul>
 */
public final class CabinetBlock extends GeolibFacingEntityBlockWithFactory<CabinetBlockEntity> {

    /** 竖向分格：0=底格（BE + Geo），1/2=上层碰撞格（仅柜子1型使用）。 */
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);

    private static final ThreadLocal<Boolean> SUPPRESS_SIBLING_BREAK = ThreadLocal.withInitial(() -> false);

    private final CabinetKind kind;

    public CabinetBlock(BlockBehaviour.Properties properties, CabinetKind kind) {
        super(properties, CabinetBlockEntity::new);
        this.kind = kind;
        registerDefaultState(defaultBlockState().setValue(PART, 0));
    }

    public CabinetKind kind() {
        return kind;
    }

    public static BlockPos masterPos(BlockState state, BlockPos pos) {
        return pos.below(state.getValue(PART));
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(PART) == 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState base = super.getStateForPlacement(context);
        if (base == null) {
            return null;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        var border = level.getWorldBorder();
        int parts = kind.columnParts();
        for (int i = 0; i < parts; i++) {
            BlockPos p = pos.above(i);
            if (!border.isWithinBounds(p) || !level.getBlockState(p).canBeReplaced(context)) {
                return null;
            }
        }
        return base.setValue(PART, 0);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!isMaster(state)) {
            return;
        }
        int parts = kind.columnParts();
        for (int i = 1; i < parts; i++) {
            level.setBlock(pos.above(i), state.setValue(PART, i), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            return true;
        }
        if (part >= kind.columnParts()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.is(this)
                && below.getValue(FACING) == state.getValue(FACING)
                && below.getValue(PART) == part - 1;
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
            if (!level.isClientSide() && !SUPPRESS_SIBLING_BREAK.get()) {
                destroyColumn(level, state, currentPos);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (!isMaster(state)) {
            return null;
        }
        return super.newBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isMaster(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    private VoxelShape shape(BlockState state) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                CabinetCollisionShapes.north(kind), state.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shape(state);
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        BlockPos master = masterPos(state, pos);
        CabinetBlockEntity be = blockEntity(level, master);
        if (be == null) {
            return InteractionResult.FAIL;
        }
        CabinetSlot slot = resolveSlot(state, master, hit);
        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty()) {
            if (be.isEmpty(slot)) {
                return InteractionResult.FAIL;
            }
            if (player.isShiftKeyDown()) {
                ItemStack taken = be.takeItem(slot);
                if (taken.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                level.playSound(null, master, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
                return InteractionResult.CONSUME;
            }
            if (!be.rotateItem(slot)) {
                return InteractionResult.FAIL;
            }
            level.playSound(null, master, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            return InteractionResult.CONSUME;
        }

        if (!be.placeItem(slot, held)) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, master, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
        return InteractionResult.CONSUME;
    }

    /**
     * 柜子1型：竖向三格与三层一一对应（点哪格进哪层）。
     * 柜子2型：单格内按命中高度选最近层腔。
     */
    private CabinetSlot resolveSlot(BlockState state, BlockPos master, BlockHitResult hit) {
        if (kind.columnParts() > 1) {
            return CabinetSlot.byIndex(state.getValue(PART));
        }
        return kind.slotFromLocalY(hit.getLocation().y - master.getY());
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos master = masterPos(state, pos);
            if (!isMaster(state) && !player.isCreative()) {
                Block.popResource(level, master, new ItemStack(this));
            }
            destroyColumnSiblings(level, state, pos, player);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private void destroyColumnSiblings(Level level, BlockState state, BlockPos brokenPos, @Nullable Player player) {
        boolean was = SUPPRESS_SIBLING_BREAK.get();
        SUPPRESS_SIBLING_BREAK.set(true);
        try {
            BlockPos master = masterPos(state, brokenPos);
            int parts = kind.columnParts();
            for (int i = 0; i < parts; i++) {
                BlockPos p = master.above(i);
                if (p.equals(brokenPos)) {
                    continue;
                }
                BlockState other = level.getBlockState(p);
                if (other.is(this)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                    if (player != null) {
                        level.levelEvent(player, 2001, p, Block.getId(other));
                    }
                }
            }
        } finally {
            SUPPRESS_SIBLING_BREAK.set(was);
        }
    }

    private void destroyColumn(LevelAccessor level, BlockState state, BlockPos anyPos) {
        boolean was = SUPPRESS_SIBLING_BREAK.get();
        SUPPRESS_SIBLING_BREAK.set(true);
        try {
            BlockPos master = masterPos(state, anyPos);
            int parts = kind.columnParts();
            for (int i = 0; i < parts; i++) {
                BlockPos p = master.above(i);
                BlockState other = level.getBlockState(p);
                if (other.is(this)) {
                    level.destroyBlock(p, false);
                }
            }
        } finally {
            SUPPRESS_SIBLING_BREAK.set(was);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (!isMaster(state)) {
            return List.of();
        }
        return super.getDrops(state, params);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(this);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isMaster(state)) {
            CabinetBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                be.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    private static CabinetBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof CabinetBlockEntity cabinet ? cabinet : null;
    }
}
