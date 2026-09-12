package org.lanye.fantasy_furniture.content.furniture.cabinet.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetStackFloors;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetShelfDebugActions;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.CabinetItemClientPick;
import org.lanye.reverie_core.content.fantasy_core.item.FantasyDebugStickItem;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 柜子：四向放置；各槽可放入一件任意物品（展示框式）。
 *
 * <ul>
 *   <li>持物右击空槽 → 放入 1 件
 *   <li>空手右击有物槽 → 展品绕竖直轴 +45°（类似展示框）
 *   <li>潜行空手右击有物槽 → 取出
 *   <li>柜子1型：geo 高 48 → 竖向三格 {@link #PART}（0 底/BE+Geo，1/2 仅碰撞）；点哪格进哪层
 *   <li>柜子2型：单格；3×3 九槽，按命中点映射最近格
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
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof FantasyDebugStickItem) {
            var msg = CabinetShelfDebugActions.tryRemoveShelf(
                    level, pos, player, hit, player.isShiftKeyDown());
            if (msg.isPresent()) {
                player.displayClientMessage(msg.get(), true);
                return InteractionResult.CONSUME;
            }
            // 持调试棒时不放置/旋转展品
            return InteractionResult.FAIL;
        }
        // Shift + crosshair on displayed item → take (empty or holding).
        if (player.isShiftKeyDown()) {
            int aimedItem = CabinetItemPicks.pickOccupiedSlot(
                    be, state.getValue(FACING), player.getEyePosition(1.0f), player.getViewVector(1.0f));
            if (aimedItem >= 0) {
                ItemStack taken = be.takeItem(aimedItem);
                if (taken.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                level.playSound(null, master, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
                return InteractionResult.CONSUME;
            }
        }

        if (held.isEmpty()) {
            int slot = resolveSlot(state, master, player, hit, be);
            if (be.isEmpty(slot)) {
                return InteractionResult.FAIL;
            }
            // Empty + Shift without aimed exhibit: take via grid/slot fallback (legacy).
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

        // Holding: empty aimed design-grid cell -> place there; aiming at an existing exhibit
        // (or clicking an open cavity that already has a stack) -> free-stack ON TOP if remaining
        // height fits at design-cell size. Occupied shelved single-cell -> reject (no dump).
        // Rejects must CONSUME so BlockItem does not fall through to vanilla world place.
        int aimedItem = CabinetItemPicks.pickOccupiedSlot(
                be, state.getValue(FACING), player.getEyePosition(1.0f), player.getViewVector(1.0f));
        if (aimedItem < 0) {
            int designSlot = resolveDesignGridSlot(state, master, player, hit);
            int cavityTop = topOccupiedInSameOpenCavity(be, designSlot);
            if (cavityTop >= 0) {
                aimedItem = cavityTop;
            } else if (be.isEmpty(designSlot)) {
                return tryPlaceHeld(be, designSlot, held, player, level, master);
            } else {
                return rejectNoCapacity(player);
            }
        }
        int free = nextOpenStackSlotAbove(be, aimedItem);
        if (free < 0) {
            return rejectNoCapacity(player);
        }
        return tryPlaceHeld(be, free, held, player, level, master);
    }

    private static InteractionResult tryPlaceHeld(
            CabinetBlockEntity be,
            int placeSlot,
            ItemStack held,
            Player player,
            Level level,
            BlockPos master) {
        if (!canFreeStackPlace(be, placeSlot, held)) {
            return rejectNoCapacity(player);
        }
        if (!be.placeItem(placeSlot, held)) {
            return rejectNoCapacity(player);
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, master, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
        return InteractionResult.CONSUME;
    }

    /** Capacity / occupied reject: show tip and consume so vanilla BlockItem cannot place. */
    private static InteractionResult rejectNoCapacity(Player player) {
        player.displayClientMessage(
                Component.translatable("message.fantasy_furniture.cabinet.no_capacity"), true);
        return InteractionResult.CONSUME;
    }

    /**
     * Prefer aimed displayed item (any storage slot); else design-grid hit (PART / slotFromHit).
     */
    private int resolveSlot(
            BlockState state, BlockPos master, Player player, BlockHitResult hit, CabinetBlockEntity be) {
        int aimedItem = CabinetItemPicks.pickOccupiedSlot(
                be, state.getValue(FACING), player.getEyePosition(1.0f), player.getViewVector(1.0f));
        if (aimedItem >= 0) {
            return aimedItem;
        }
        return resolveGridSlot(state, master, player, hit, be);
    }

    /** Design-grid slot only (empty-hand fallback). Clamped to {@link CabinetKind#slotCount()}. */
    private int resolveGridSlot(
            BlockState state, BlockPos master, Player player, BlockHitResult hit, CabinetBlockEntity be) {
        int aimed = CabinetItemPicks.pickOccupiedSlot(
                be, state.getValue(FACING), player.getEyePosition(1.0f), player.getViewVector(1.0f));
        if (aimed >= 0 && aimed < kind.slotCount()) {
            return aimed;
        }
        return resolveDesignGridSlot(state, master, player, hit);
    }

    /** PART / slotFromHit only — never prefers an occupied exhibit AABB. */
    private int resolveDesignGridSlot(BlockState state, BlockPos master, Player player, BlockHitResult hit) {
        if (kind.columnParts() > 1) {
            return CabinetSlot.clampIndex(state.getValue(PART), kind.slotCount());
        }
        return kind.slotFromHit(
                master,
                state.getValue(FACING),
                hit,
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f));
    }

    /**
     * Topmost occupied exhibit in the same undivided cavity as {@code slotHint} (design grid or
     * storage). Used when the crosshair misses exhibit AABBs but hits the cabinet PART / cell.
     */
    private static int topOccupiedInSameOpenCavity(CabinetBlockEntity be, int slotHint) {
        CabinetKind kind = be.kind();
        int col = kind.columnOfStorage(slotHint);
        float hintY = cavityHintY(be, kind, slotHint);
        int top = -1;
        float topY = Float.NEGATIVE_INFINITY;
        int levels = be.maxLevelsPerColumn();
        for (int lvl = 0; lvl < levels; lvl++) {
            int s = kind.slotAt(col, lvl);
            if (be.isEmpty(s)) {
                continue;
            }
            CabinetStackFloors.SlotPose pose =
                    CabinetStackFloors.pose(be, s, CabinetBlock::estimatedStackHeight);
            float mid = pose.floorY() + Math.max(0f, pose.renderedH()) * 0.5f;
            if (!sameOpenCavityY(be, kind, hintY, mid)) {
                continue;
            }
            float at = pose.floorY() + Math.max(0f, pose.renderedH());
            if (at > topY) {
                topY = at;
                top = s;
            }
        }
        return top;
    }

    /** Y inside the cavity that contains {@code slotHint} (occupied mid, or design shelf line). */
    private static float cavityHintY(CabinetBlockEntity be, CabinetKind kind, int slotHint) {
        if (!be.isEmpty(slotHint)) {
            CabinetStackFloors.SlotPose pose =
                    CabinetStackFloors.pose(be, slotHint, CabinetBlock::estimatedStackHeight);
            return pose.floorY() + Math.max(0f, pose.renderedH()) * 0.5f;
        }
        int row = Math.min(Math.max(0, kind.rowOf(slotHint)), Math.max(0, kind.rows() - 1));
        return kind.shelfTopY(kind.slotAt(kind.columnOfStorage(slotHint), row))
                + CabinetKind.SHELF_CLEARANCE;
    }

    /** True when no present shelf bottom lies strictly between {@code y1} and {@code y2}. */
    private static boolean sameOpenCavityY(CabinetBlockEntity be, CabinetKind kind, float y1, float y2) {
        float lo = Math.min(y1, y2);
        float hi = Math.max(y1, y2);
        for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
            if (!be.isShelfPresent(si)) {
                continue;
            }
            float minY = (float) kind.shelfLocalAabb(si).minY;
            if (minY > lo + 1.0e-4f && minY < hi - 1.0e-4f) {
                return false;
            }
        }
        return true;
    }

    /**
     * Next empty storage slot in the same undivided cavity above {@code aimedSlot}, skipping
     * design cells that still sit on a present shelf (those are other compartments).
     */
    private static int nextOpenStackSlotAbove(CabinetBlockEntity be, int aimedSlot) {
        CabinetKind kind = be.kind();
        int cols = Math.max(1, kind.cols());
        int col = kind.columnOfStorage(aimedSlot);
        int levels = be.maxLevelsPerColumn();
        for (int lvl = 0; lvl < levels; lvl++) {
            int free = kind.slotAt(col, lvl);
            if (!be.isEmpty(free)) {
                continue;
            }
            if (isOpenStackAbove(be, aimedSlot, free)) {
                return free;
            }
        }
        return -1;
    }

    /**
     * Whether {@code freeSlot} lies in the same undivided vertical cavity above {@code aimedSlot}.
     *
     * <p>Uses shelf geometry (not design-row index): a present supporting shelf for {@code freeSlot}
     * whose floor is above the aimed exhibit means a different compartment — skip that slot and use
     * a denser free-stack level instead. Otherwise reject only if a present shelf bottom sits
     * strictly between aimed top and free floor.
     */
    private static boolean isOpenStackAbove(CabinetBlockEntity be, int aimedSlot, int freeSlot) {
        CabinetKind kind = be.kind();
        int cols = Math.max(1, kind.cols());
        if (kind.columnOfStorage(aimedSlot) != kind.columnOfStorage(freeSlot)) {
            return false;
        }
        int aimedLevel = Math.floorDiv(aimedSlot, cols);
        int freeLevel = Math.floorDiv(freeSlot, cols);
        if (freeLevel <= aimedLevel) {
            return false;
        }

        CabinetStackFloors.SlotPose aimedPose =
                CabinetStackFloors.pose(be, aimedSlot, CabinetBlock::estimatedStackHeight);
        float aimedTop = aimedPose.floorY() + Math.max(0f, aimedPose.renderedH());

        int freeShelf = kind.shelfSupportingSlot(freeSlot);
        if (freeShelf >= 0 && be.isShelfPresent(freeShelf)) {
            // Empty design cell on a present shelf above the aimed stack → other compartment.
            if (kind.itemFloorY(freeSlot) > aimedTop + 1.0e-3f) {
                return false;
            }
        }

        CabinetStackFloors.SlotPose freePose =
                CabinetStackFloors.pose(be, freeSlot, CabinetBlock::estimatedStackHeight);
        float freeFloor = freePose.floorY();
        for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
            if (!be.isShelfPresent(si)) {
                continue;
            }
            float minY = (float) kind.shelfLocalAabb(si).minY;
            if (minY > aimedTop + 1.0e-4f && minY < freeFloor - 1.0e-4f) {
                return false;
            }
        }
        return true;
    }

    /**
     * Capacity: item at design-cell (shelved single-compartment) size must fit under the
     * next present shelf / lid above the free slot floor. Remaining height is a reject
     * budget, not a shrink target.
     */
    private static boolean canFreeStackPlace(CabinetBlockEntity be, int freeSlot, ItemStack held) {
        CabinetKind kind = be.kind();
        CabinetStackFloors.SlotPose pose =
                CabinetStackFloors.pose(be, freeSlot, CabinetBlock::estimatedStackHeight);
        float floorY = pose.floorY();
        float ceiling = CabinetStackFloors.ceilingAbove(be, kind, floorY);
        float remaining = ceiling - floorY - CabinetKind.SHELF_CLEARANCE;
        if (remaining <= CabinetItemPicks.MIN_EXTENT) {
            return false;
        }
        CabinetKind.CavityFit base = kind.cavityFit(freeSlot);
        CabinetItemPicks.Size size = CabinetItemPicks.estimateSize(held, base);
        return size.height() <= remaining + 1.0e-3f;
    }

    private static float estimatedStackHeight(CabinetBlockEntity be, int slot, float fitH) {
        ItemStack stack = be.getItem(slot);
        if (stack.isEmpty()) {
            return 0f;
        }
        CabinetKind kind = be.kind();
        CabinetKind.CavityFit base = kind.cavityFit(slot);
        return CabinetItemPicks.estimateSize(
                        stack, new CabinetKind.CavityFit(base.width(), fitH, base.depth()))
                .height();
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
        if (level instanceof Level l && l.isClientSide()) {
            return CabinetItemClientPick.resolveCloneItemStack(l, state, pos);
        }
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
