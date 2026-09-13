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
import net.minecraft.world.item.BlockItem;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.cabinet.Cabinet1OpenColumn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetAppearance;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetMaterials;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetStackFloors;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetShelfDebugActions;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.CabinetItemClientPick;
import org.lanye.fantasy_furniture.content.furniture.cabinet.item.CabinetBlockItem;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
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
 *   <li>潜行持方块右击（未对准展品）→ 不放展品，回退原版放置
 *   <li>柜子1型：每次放 1 格；对准同朝向顶面向上续放，同柱最多 {@link CabinetKind#CABINET_1_MAX_STACK} 格相连；
 *       超出后新格正常放置为新柱起点；{@link #SEGMENT} 由上下邻接刷新（alone/2x/2z/2s）；
 *       {@link #SIDE_OPEN_NEG}/{@link #SIDE_OPEN_POS}：局部 −X/+X 有同朝向邻柜时去该侧隔板，
 *       两柱并排时双方都变，开口相对相连
 *   <li>柜子2型：单格；3×3 九槽，按命中点映射最近格；{@link #SEGMENT} 恒 alone
 * </ul>
 */
public final class CabinetBlock extends GeolibFacingEntityBlockWithFactory<CabinetBlockEntity> {

    /** 竖向拼装角色（柜子1 邻接刷新；柜子2 恒 alone）。 */
    public static final EnumProperty<CabinetSegment> SEGMENT =
            EnumProperty.create("segment", CabinetSegment.class);

    /**
     * 去侧面：北向局部 −X（{@link Direction#getCounterClockWise()}）有同朝向邻柜。
     * 使用 {@code cabinet_1_open_*}（缺 −X）。柜子2 恒 false。
     */
    public static final BooleanProperty SIDE_OPEN_NEG = BooleanProperty.create("side_open_neg");

    /**
     * 去侧面：北向局部 +X（{@link Direction#getClockWise()}）有同朝向邻柜。
     * 使用 {@code cabinet_1_open_px_*}（缺 +X）。柜子2 恒 false。
     */
    public static final BooleanProperty SIDE_OPEN_POS = BooleanProperty.create("side_open_pos");

    /** 材质档（1～{@link CabinetMaterials#MAX_COUNT}；按 kind 钳制有效色数）。 */
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, CabinetMaterials.MAX_COUNT);

    private final CabinetKind kind;

    public CabinetBlock(BlockBehaviour.Properties properties, CabinetKind kind) {
        super(properties, CabinetBlockEntity::new);
        this.kind = kind;
        registerDefaultState(
                defaultBlockState()
                        .setValue(SEGMENT, CabinetSegment.ALONE)
                        .setValue(SIDE_OPEN_NEG, false)
                        .setValue(SIDE_OPEN_POS, false)
                        .setValue(MATERIAL, CabinetMaterials.DEFAULT));
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
        builder.add(SEGMENT, SIDE_OPEN_NEG, SIDE_OPEN_POS, MATERIAL);
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
        int material =
                CabinetMaterials.clamp(
                        kind, CabinetAppearance.fromStack(context.getItemInHand(), kind).materialId());
        base = base.setValue(MATERIAL, material);
        Direction facing = base.getValue(FACING);
        if (kind == CabinetKind.CABINET_1) {
            BlockState below = level.getBlockState(pos.below());
            if (below.getBlock() instanceof CabinetBlock belowCab && belowCab.kind == CabinetKind.CABINET_1) {
                // 续放：强制继承下方朝向；若下方柱已满 3 格，本格仍可放置但不成柱相连
                facing = below.getValue(FACING);
                base = base.setValue(FACING, facing);
            }
            return base.setValue(SEGMENT, computeSegment(level, pos, facing))
                    .setValue(SIDE_OPEN_NEG, hasSideNeighbor(level, pos, facing.getCounterClockWise(), facing))
                    .setValue(SIDE_OPEN_POS, hasSideNeighbor(level, pos, facing.getClockWise(), facing));
        }
        return base.setValue(SEGMENT, CabinetSegment.ALONE)
                .setValue(SIDE_OPEN_NEG, false)
                .setValue(SIDE_OPEN_POS, false);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        int material =
                CabinetMaterials.clamp(kind, CabinetAppearance.fromStack(stack, kind).materialId());
        BlockState colored = state.setValue(MATERIAL, material);
        if (colored != state) {
            level.setBlock(pos, colored, Block.UPDATE_ALL);
            state = colored;
        }
        if (level.isClientSide() || kind != CabinetKind.CABINET_1) {
            return;
        }
        refreshCabinetNeighbors(level, pos, state.getValue(FACING));
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
        if (kind != CabinetKind.CABINET_1) {
            return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }
        Direction facing = state.getValue(FACING);
        BlockState next = state;
        if (direction.getAxis() == Direction.Axis.Y) {
            CabinetSegment seg = computeSegment(level, currentPos, facing);
            if (next.getValue(SEGMENT) != seg) {
                next = next.setValue(SEGMENT, seg);
            }
        }
        if (direction.getAxis().isHorizontal()) {
            boolean openNeg = hasSideNeighbor(level, currentPos, facing.getCounterClockWise(), facing);
            boolean openPos = hasSideNeighbor(level, currentPos, facing.getClockWise(), facing);
            if (next.getValue(SIDE_OPEN_NEG) != openNeg) {
                next = next.setValue(SIDE_OPEN_NEG, openNeg);
            }
            if (next.getValue(SIDE_OPEN_POS) != openPos) {
                next = next.setValue(SIDE_OPEN_POS, openPos);
            }
        }
        return next;
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

    /** 下方同朝向柜体（材质可不同）；用于物理邻接探测。 */
    private static boolean isSameColumnCell(LevelReader level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CabinetBlock cabinet) || cabinet.kind != CabinetKind.CABINET_1) {
            return false;
        }
        return state.getValue(FACING) == facing;
    }

    /**
     * 自连续同朝向柱底向上的 0-based 序号；非本柱格返回 -1。
     * 每 {@link CabinetKind#CABINET_1_MAX_STACK} 格为一节，第四节起属新柱。
     */
    private static int columnRankFromBottom(LevelReader level, BlockPos pos, Direction facing) {
        if (!isSameColumnCell(level, pos, facing)) {
            return -1;
        }
        int rank = 0;
        BlockPos p = pos.below();
        while (isSameColumnCell(level, p, facing)) {
            rank++;
            p = p.below();
        }
        return rank;
    }

    /**
     * 两格是否属于同一节竖柱（同朝向且自底起落在同一 3 格分组内）。
     * 供 SEGMENT / 开口腔柱使用；满三格后再叠的邻格不相连。
     */
    public static boolean areLinkedInStack(
            LevelReader level, BlockPos pos, BlockPos neighbor, Direction facing) {
        if (!isSameColumnCell(level, neighbor, facing)) {
            return false;
        }
        int a = columnRankFromBottom(level, pos, facing);
        int b = columnRankFromBottom(level, neighbor, facing);
        if (a < 0 || b < 0) {
            return false;
        }
        int max = CabinetKind.CABINET_1_MAX_STACK;
        return a / max == b / max;
    }

    /** 指定世界方向上一格是否为同朝向柜子1。 */
    private static boolean hasSideNeighbor(
            LevelReader level, BlockPos pos, Direction side, Direction facing) {
        return isSameColumnCell(level, pos.relative(side), facing);
    }

    private static CabinetSegment computeSegment(LevelReader level, BlockPos pos, Direction facing) {
        boolean above = areLinkedInStack(level, pos, pos.above(), facing);
        boolean below = areLinkedInStack(level, pos, pos.below(), facing);
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

    /** 写入本格 SEGMENT + 双侧 SIDE_OPEN（若已是本柜）。 */
    private void applyComputedLinks(Level level, BlockPos pos, Direction facing) {
        BlockState self = level.getBlockState(pos);
        if (!self.is(this) || self.getValue(FACING) != facing) {
            return;
        }
        CabinetSegment seg = computeSegment(level, pos, facing);
        boolean openNeg = hasSideNeighbor(level, pos, facing.getCounterClockWise(), facing);
        boolean openPos = hasSideNeighbor(level, pos, facing.getClockWise(), facing);
        BlockState next = self;
        if (self.getValue(SEGMENT) != seg) {
            next = next.setValue(SEGMENT, seg);
        }
        if (self.getValue(SIDE_OPEN_NEG) != openNeg) {
            next = next.setValue(SIDE_OPEN_NEG, openNeg);
        }
        if (self.getValue(SIDE_OPEN_POS) != openPos) {
            next = next.setValue(SIDE_OPEN_POS, openPos);
        }
        if (next != self) {
            level.setBlock(pos, next, Block.UPDATE_ALL);
        }
    }

    /** 放置/换色后刷新本格与上下、水平邻格链接态。 */
    private void refreshCabinetNeighbors(Level level, BlockPos pos, Direction facing) {
        applyComputedLinks(level, pos, facing);
        for (Direction dir : new Direction[] {
            Direction.UP, Direction.DOWN, facing.getCounterClockWise(), facing.getClockWise()
        }) {
            applyComputedLinks(level, pos.relative(dir), facing);
        }
    }

    /** 刷子换色后：重算 SEGMENT/SIDE_OPEN_* 并刷新同朝向邻格（材质不影响成柱）。 */
    public void applyMaterialRecolor(Level level, BlockPos pos, BlockState recolored) {
        if (kind != CabinetKind.CABINET_1) {
            level.setBlock(pos, recolored, Block.UPDATE_ALL_IMMEDIATE);
            return;
        }
        Direction facing = recolored.getValue(FACING);
        CabinetSegment seg = computeSegment(level, pos, facing);
        boolean openNeg = hasSideNeighbor(level, pos, facing.getCounterClockWise(), facing);
        boolean openPos = hasSideNeighbor(level, pos, facing.getClockWise(), facing);
        BlockState local =
                recolored
                        .setValue(SEGMENT, seg)
                        .setValue(SIDE_OPEN_NEG, openNeg)
                        .setValue(SIDE_OPEN_POS, openPos);
        level.setBlock(pos, local, Block.UPDATE_ALL_IMMEDIATE);
        refreshCabinetNeighbors(level, pos, facing);
    }

    /**
     * 持本柜物品点顶面 → 交给 BlockItem 向上续放（含已满 3 格时另起新柱）。
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

    /**
     * 准心是否对准柜内展品（供潜行交互：持方块时决定强制取出还是放行原版放置）。
     */
    public static boolean hasAimedExhibit(Level level, BlockState state, BlockPos pos, Player player) {
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return false;
        }
        BlockPos master = masterPos(state, pos);
        CabinetBlockEntity be = blockEntity(level, master);
        if (be == null) {
            return false;
        }
        return pickAimedExhibit(level, state, pos, be, player) != null;
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.SUCCESS;
        }
        InteractionResult defer = tryDeferStackPlace(state, level, pos, player, hand, hit);
        if (defer != null) {
            return defer;
        }
        ItemStack held = player.getItemInHand(hand);
        // 潜行持方块且未对准展品：放行客户端放置预测
        if (player.isShiftKeyDown()
                && held.getItem() instanceof BlockItem
                && !hasAimedExhibit(level, state, pos, player)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
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
            // 潜行持方块且未对准展品：不放展品，回退原版放置
            if (held.getItem() instanceof BlockItem) {
                return InteractionResult.PASS;
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

        // Holding: 柜子1 开口腔自由叠；柜子2 准心格空则放一件，已占则静默拒绝（不叠、无容积提示）。
        if (kind == CabinetKind.CABINET_1) {
            return tryPlaceCabinet1Column(level, state, pos, be, held, player);
        }
        return tryPlaceCabinet2Cell(level, state, master, be, held, player, hit);
    }

    /** 柜子2：每设计格仅一件；已占 / 未命中格 → CONSUME 且无提示（防方块落到世界）。 */
    private InteractionResult tryPlaceCabinet2Cell(
            Level level,
            BlockState state,
            BlockPos master,
            CabinetBlockEntity be,
            ItemStack held,
            Player player,
            BlockHitResult hit) {
        int designSlot = resolveDesignGridSlot(state, master, player, hit);
        if (designSlot < 0 || designSlot >= be.slotCount()) {
            return InteractionResult.CONSUME;
        }
        if (!be.isEmpty(designSlot)) {
            return InteractionResult.CONSUME;
        }
        if (!be.placeItem(designSlot, held)) {
            return InteractionResult.CONSUME;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, master, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
        return InteractionResult.CONSUME;
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
        List<ItemStack> drops = super.getDrops(state, params);
        for (ItemStack stack : drops) {
            if (stack.getItem() instanceof CabinetBlockItem) {
                CabinetAppearance.writeToStack(
                        stack, new CabinetAppearance(state.getValue(MATERIAL)));
            }
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (level instanceof Level l && l.isClientSide()) {
            return CabinetItemClientPick.resolveCloneItemStack(l, state, pos);
        }
        ItemStack stack = new ItemStack(this);
        CabinetAppearance.writeToStack(stack, new CabinetAppearance(state.getValue(MATERIAL)));
        return stack;
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
                for (Direction dir : new Direction[] {
                    Direction.UP, Direction.DOWN, facing.getCounterClockWise(), facing.getClockWise()
                }) {
                    BlockPos n = pos.relative(dir);
                    BlockState ns = level.getBlockState(n);
                    if (ns.is(this) && ns.getValue(FACING) == facing) {
                        CabinetSegment seg = computeSegment(level, n, facing);
                        boolean openNeg = hasSideNeighbor(level, n, facing.getCounterClockWise(), facing);
                        boolean openPos = hasSideNeighbor(level, n, facing.getClockWise(), facing);
                        BlockState next = ns;
                        if (ns.getValue(SEGMENT) != seg) {
                            next = next.setValue(SEGMENT, seg);
                        }
                        if (ns.getValue(SIDE_OPEN_NEG) != openNeg) {
                            next = next.setValue(SIDE_OPEN_NEG, openNeg);
                        }
                        if (ns.getValue(SIDE_OPEN_POS) != openPos) {
                            next = next.setValue(SIDE_OPEN_POS, openPos);
                        }
                        if (next != ns) {
                            level.setBlock(n, next, Block.UPDATE_ALL);
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
