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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.cabinet.Cabinet1OpenColumn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetStackFloors;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetShelfDebugActions;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.CabinetItemClientPick;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;
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
 *   <li>柜子1型：每次放 1 格；对准同朝向顶面向上续放，整柱最多 {@link CabinetKind#CABINET_1_MAX_STACK}；
 *       {@link #SEGMENT} 由上下邻接刷新（alone/2x/2z/2s）
 *   <li>柜子2型：单格；3×3 九槽，按命中点映射最近格；{@link #SEGMENT} 恒 alone
 * </ul>
 */
public final class CabinetBlock extends GeolibFacingEntityBlockWithFactory<CabinetBlockEntity> {

    /** 竖向拼装角色（柜子1 邻接刷新；柜子2 恒 alone）。 */
    public static final EnumProperty<CabinetSegment> SEGMENT =
            EnumProperty.create("segment", CabinetSegment.class);

    private final CabinetKind kind;

    public CabinetBlock(BlockBehaviour.Properties properties, CabinetKind kind) {
        super(properties, CabinetBlockEntity::new);
        this.kind = kind;
        registerDefaultState(defaultBlockState().setValue(SEGMENT, CabinetSegment.ALONE));
    }

    public CabinetKind kind() {
        return kind;
    }

    /** 每格独立 BE；兼容旧调用名，恒返回本格。 */
    public static BlockPos masterPos(BlockState state, BlockPos pos) {
        return pos;
    }

    public static boolean isMaster(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEGMENT);
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
        if (!level.getWorldBorder().isWithinBounds(pos) || !level.getBlockState(pos).canBeReplaced(context)) {
            return null;
        }
        Direction facing = base.getValue(FACING);
        if (kind == CabinetKind.CABINET_1) {
            BlockState below = level.getBlockState(pos.below());
            if (below.getBlock() instanceof CabinetBlock belowCab && belowCab.kind == CabinetKind.CABINET_1) {
                // 续放：强制继承下方朝向，避免玩家朝向不一致拆柱
                facing = below.getValue(FACING);
                base = base.setValue(FACING, facing);
                if (!canStackAt(level, pos, facing)) {
                    return null;
                }
            }
        }
        return base.setValue(SEGMENT, computeSegment(level, pos, facing));
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide() || kind != CabinetKind.CABINET_1) {
            return;
        }
        refreshSegmentNeighbors(level, pos, state.getValue(FACING));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        if (kind != CabinetKind.CABINET_1 || direction.getAxis() != Direction.Axis.Y) {
            return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }
        CabinetSegment next = computeSegment(level, currentPos, state.getValue(FACING));
        if (state.getValue(SEGMENT) != next) {
            return state.setValue(SEGMENT, next);
        }
        return state;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return super.newBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    /** 下方同朝向柱高（含将放置格）是否 ≤ 上限。 */
    private boolean canStackAt(LevelReader level, BlockPos placePos, Direction facing) {
        BlockPos below = placePos.below();
        if (!isSameColumnCell(level, below, facing)) {
            return true;
        }
        int belowHeight = contiguousHeightDown(level, below, facing);
        return belowHeight + 1 <= CabinetKind.CABINET_1_MAX_STACK;
    }

    private static int contiguousHeightDown(LevelReader level, BlockPos from, Direction facing) {
        int h = 0;
        BlockPos p = from;
        while (h < CabinetKind.CABINET_1_MAX_STACK + 2 && isSameColumnCell(level, p, facing)) {
            h++;
            p = p.below();
        }
        return h;
    }

    private static boolean isSameColumnCell(LevelReader level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CabinetBlock cabinet) || cabinet.kind != CabinetKind.CABINET_1) {
            return false;
        }
        return state.getValue(FACING) == facing;
    }

    private static CabinetSegment computeSegment(LevelReader level, BlockPos pos, Direction facing) {
        boolean above = isSameColumnCell(level, pos.above(), facing);
        boolean below = isSameColumnCell(level, pos.below(), facing);
        if (!above && !below) {
            return CabinetSegment.ALONE;
        }
        if (!below && above) {
            return CabinetSegment.BOTTOM;
        }
        if (below && !above) {
            return CabinetSegment.TOP;
        }
        return CabinetSegment.MIDDLE;
    }

    /** 放置后刷新本格与上下邻格 SEGMENT（触发对方 updateShape）。 */
    private void refreshSegmentNeighbors(Level level, BlockPos pos, Direction facing) {
        BlockState self = level.getBlockState(pos);
        if (self.is(this)) {
            CabinetSegment seg = computeSegment(level, pos, facing);
            if (self.getValue(SEGMENT) != seg) {
                level.setBlock(pos, self.setValue(SEGMENT, seg), Block.UPDATE_ALL);
            }
        }
        for (Direction dir : new Direction[] {Direction.UP, Direction.DOWN}) {
            BlockPos n = pos.relative(dir);
            BlockState ns = level.getBlockState(n);
            if (ns.is(this) && ns.getValue(FACING) == facing) {
                CabinetSegment seg = computeSegment(level, n, facing);
                if (ns.getValue(SEGMENT) != seg) {
                    level.setBlock(n, ns.setValue(SEGMENT, seg), Block.UPDATE_ALL);
                }
            }
        }
    }

    /**
     * 持本柜物品点顶面 → 交给 BlockItem 向上续放；已达 3 格则消费并提示。
     * 返回 null 表示不 defer，继续柜内交互。
     */
    @Nullable
    private InteractionResult tryDeferStackPlace(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind != CabinetKind.CABINET_1 || hit.getDirection() != Direction.UP) {
            return null;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(asItem())) {
            return null;
        }
        Direction facing = state.getValue(FACING);
        int height = contiguousHeightDown(level, pos, facing);
        if (height >= CabinetKind.CABINET_1_MAX_STACK) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.fantasy_furniture.cabinet.no_capacity"), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
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
        InteractionResult defer = tryDeferStackPlace(state, level, pos, player, hand, hit);
        if (defer != null) {
            return defer;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        InteractionResult defer = tryDeferStackPlace(state, level, pos, player, hand, hit);
        if (defer != null) {
            return defer;
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
            Cabinet1OpenColumn.Aimed aimedTake = pickAimedExhibit(level, state, pos, be, player);
            if (aimedTake != null) {
                ItemStack taken = aimedTake.be().takeItem(aimedTake.slot());
                if (taken.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                level.playSound(
                        null,
                        aimedTake.be().getBlockPos(),
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS,
                        0.8f,
                        1.0f);
                return InteractionResult.CONSUME;
            }
        }

        if (held.isEmpty()) {
            Cabinet1OpenColumn.Aimed aimedEmpty = pickAimedExhibit(level, state, pos, be, player);
            if (aimedEmpty == null) {
                int slot = resolveSlot(state, master, player, hit, be);
                if (be.isEmpty(slot)) {
                    return InteractionResult.FAIL;
                }
                aimedEmpty = new Cabinet1OpenColumn.Aimed(be, slot);
            }
            if (aimedEmpty.be().isEmpty(aimedEmpty.slot())) {
                return InteractionResult.FAIL;
            }
            // Empty + Shift without aimed exhibit: take via grid/slot fallback (legacy).
            if (player.isShiftKeyDown()) {
                ItemStack taken = aimedEmpty.be().takeItem(aimedEmpty.slot());
                if (taken.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                level.playSound(
                        null,
                        aimedEmpty.be().getBlockPos(),
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS,
                        0.8f,
                        1.0f);
                return InteractionResult.CONSUME;
            }
            if (!aimedEmpty.be().rotateItem(aimedEmpty.slot())) {
                return InteractionResult.FAIL;
            }
            level.playSound(
                    null,
                    aimedEmpty.be().getBlockPos(),
                    SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.BLOCKS,
                    0.8f,
                    1.0f);
            return InteractionResult.CONSUME;
        }

        // Holding: empty aimed design-grid cell -> place there; aiming at an existing exhibit
        // (or clicking an open cavity that already has a stack) -> free-stack ON TOP if remaining
        // height fits at design-cell size. Occupied shelved single-cell -> reject (no dump).
        // Rejects must CONSUME so BlockItem does not fall through to vanilla world place.
        // 柜子1：拆中隔后开口腔柱跨格叠放。
        if (kind == CabinetKind.CABINET_1) {
            return tryPlaceCabinet1Column(level, state, pos, be, held, player);
        }
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

    @Nullable
    private static Cabinet1OpenColumn.Aimed pickAimedExhibit(
            Level level, BlockState state, BlockPos pos, CabinetBlockEntity be, Player player) {
        Direction facing = state.getValue(CabinetBlock.FACING);
        if (be.kind() == CabinetKind.CABINET_1) {
            return Cabinet1OpenColumn.pickOccupied(
                    level, pos, facing, player.getEyePosition(1.0f), player.getViewVector(1.0f));
        }
        int slot =
                CabinetItemPicks.pickOccupiedSlot(
                        be, facing, player.getEyePosition(1.0f), player.getViewVector(1.0f));
        return slot < 0 ? null : new Cabinet1OpenColumn.Aimed(be, slot);
    }

    private InteractionResult tryPlaceCabinet1Column(
            Level level,
            BlockState state,
            BlockPos pos,
            CabinetBlockEntity hitBe,
            ItemStack held,
            Player player) {
        Direction facing = state.getValue(FACING);
        var cells = Cabinet1OpenColumn.cellsBottomToTop(level, pos);
        Cabinet1OpenColumn.Aimed aimed =
                Cabinet1OpenColumn.pickOccupied(
                        level, pos, facing, player.getEyePosition(1.0f), player.getViewVector(1.0f));
        if (aimed == null) {
            float hintWorld = pos.getY() + hitBe.cavityBaseFloorY();
            aimed = Cabinet1OpenColumn.topOccupied(cells, hintWorld, CabinetBlock::estimatedStackHeight);
            if (aimed == null) {
                CabinetBlockEntity placeBe = cells.isEmpty() ? hitBe : cells.get(0);
                if (!placeBe.isEmpty(0)) {
                    return rejectNoCapacity(player);
                }
                return tryPlaceHeld(placeBe, 0, held, player, level, placeBe.getBlockPos());
            }
        }
        Cabinet1OpenColumn.Aimed free =
                Cabinet1OpenColumn.nextOpenStackAbove(aimed, CabinetBlock::estimatedStackHeight);
        if (free == null) {
            return rejectNoCapacity(player);
        }
        return tryPlaceHeld(free.be(), free.slot(), held, player, level, free.be().getBlockPos());
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
     * Prefer aimed displayed item (any storage slot); else design-grid hit.
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

    /** Design grid：柜子1 单格恒槽 0；柜子2 用 slotFromHit。 */
    private int resolveDesignGridSlot(BlockState state, BlockPos master, Player player, BlockHitResult hit) {
        if (kind == CabinetKind.CABINET_1) {
            return 0;
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
            if (!be.isShelfActive(si)) {
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
        if (freeShelf >= 0 && be.isShelfActive(freeShelf)) {
            // Empty design cell on a present shelf above the aimed stack → other compartment.
            if (kind.itemFloorY(freeSlot) > aimedTop + 1.0e-3f) {
                return false;
            }
        }

        CabinetStackFloors.SlotPose freePose =
                CabinetStackFloors.pose(be, freeSlot, CabinetBlock::estimatedStackHeight);
        float freeFloor = freePose.floorY();
        for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
            if (!be.isShelfActive(si)) {
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
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
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
        if (!state.is(newState.getBlock())) {
            CabinetBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                be.dropContents();
            }
            if (!level.isClientSide() && kind == CabinetKind.CABINET_1) {
                Direction facing = state.getValue(FACING);
                // 邻格 SEGMENT 由 updateShape 在破坏后刷新；此处主动推一次更稳
                for (Direction dir : new Direction[] {Direction.UP, Direction.DOWN}) {
                    BlockPos n = pos.relative(dir);
                    BlockState ns = level.getBlockState(n);
                    if (ns.is(this) && ns.getValue(FACING) == facing) {
                        CabinetSegment seg = computeSegment(level, n, facing);
                        if (ns.getValue(SEGMENT) != seg) {
                            level.setBlock(n, ns.setValue(SEGMENT, seg), Block.UPDATE_ALL);
                        }
                    }
                }
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
