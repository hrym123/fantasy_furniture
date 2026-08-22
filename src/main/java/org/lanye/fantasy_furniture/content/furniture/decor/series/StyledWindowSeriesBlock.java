package org.lanye.fantasy_furniture.content.furniture.decor.series;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.lanye.fantasy_furniture.bootstrap.block.StyledWindowSeriesRegistration;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.geolib.multiblock.WallPlaneFootprint;
import org.lanye.reverie_core.geolib.multiblock.WallPlanePartProperties;
import org.lanye.reverie_core.geolib.multiblock.WallPlanePlacement;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 型号窗 1～7：墙面多格足迹；{@link #FACING}×{@link #SHAPE}×{@link #PART_U}/{@link #PART_V}；
 * 颜色由 block id / {@link #colorIndex} 表达（REG-608）。仅底左主格有 BE。
 */
public class StyledWindowSeriesBlock
        extends GeolibFacingEntityBlockWithFactory<StyledWindowSeriesBlockEntity> {

    private static final ThreadLocal<Player> BREAKING_PLAYER = new ThreadLocal<>();

    public static final IntegerProperty SHAPE =
            IntegerProperty.create("shape", 0, StyledWindowSeriesCatalog.SHAPE_PROPERTY_MAX);
    public static final IntegerProperty PART_U = WallPlanePartProperties.PART_U;
    public static final IntegerProperty PART_V = WallPlanePartProperties.PART_V;

    private final StyledWindowSeriesId seriesId;
    private final int colorIndex;

    public StyledWindowSeriesBlock(
            BlockBehaviour.Properties properties, StyledWindowSeriesId seriesId, int colorIndex) {
        super(
                properties,
                (pos, state) -> new StyledWindowSeriesBlockEntity(seriesId, pos, state));
        this.seriesId = seriesId;
        this.colorIndex = colorIndex;
        registerDefaultState(
                defaultBlockState()
                        .setValue(SHAPE, 0)
                        .setValue(PART_U, 0)
                        .setValue(PART_V, 0));
    }

    public StyledWindowSeriesId seriesId() {
        return seriesId;
    }

    public int colorIndex() {
        return colorIndex;
    }

    public StyledWindowSeriesSpec spec() {
        return StyledWindowSeriesCatalog.get(seriesId);
    }

    public static StyledWindowSeriesId seriesIdOf(BlockState state) {
        if (state.getBlock() instanceof StyledWindowSeriesBlock b) {
            return b.seriesId;
        }
        return StyledWindowSeriesId.W1;
    }

    public static int colorIndexOf(BlockState state) {
        if (state.getBlock() instanceof StyledWindowSeriesBlock b) {
            return b.colorIndex;
        }
        return 0;
    }

    public static boolean isMaster(BlockState state) {
        return WallPlanePartProperties.isMaster(state.getValue(PART_U), state.getValue(PART_V));
    }

    public static BlockPos masterPos(BlockState state, BlockPos pos) {
        StyledWindowSeriesSpec spec = ((StyledWindowSeriesBlock) state.getBlock()).spec();
        return spec.footprint()
                .masterFromPart(
                        pos,
                        state.getValue(FACING),
                        state.getValue(PART_U),
                        state.getValue(PART_V));
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        StyledWindowSeriesBlockClientExtensions.register(consumer);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE, PART_U, PART_V);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        WallPlaneFootprint fp = spec().footprint();
        BlockPos origin = WallPlanePlacement.tryOriginForPlacement(context, facing, fp);
        if (origin == null) {
            return null;
        }
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(SHAPE, 0)
                .setValue(PART_U, 0)
                .setValue(PART_V, 0);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!isMaster(state)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        WallPlaneFootprint fp = spec().footprint();
        int shape = state.getValue(SHAPE);
        WallPlanePlacement.placeSiblings(
                level,
                pos,
                facing,
                fp,
                state,
                (u, v) ->
                        state.setValue(PART_U, u)
                                .setValue(PART_V, v)
                                .setValue(SHAPE, shape));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        WallPlaneFootprint fp = spec().footprint();
        Direction facing = state.getValue(FACING);
        int u = state.getValue(PART_U);
        int v = state.getValue(PART_V);
        if (!fp.containsPart(u, v)) {
            return false;
        }
        if (isMaster(state)) {
            return true;
        }
        BlockPos master = fp.masterFromPart(pos, facing, u, v);
        BlockState masterState = level.getBlockState(master);
        return sameWindow(masterState, state) && isMaster(masterState);
    }

    private boolean sameWindow(BlockState a, BlockState b) {
        return a.getBlock() instanceof StyledWindowSeriesBlock ba
                && b.getBlock() instanceof StyledWindowSeriesBlock bb
                && ba.seriesId == bb.seriesId
                && ba.colorIndex == bb.colorIndex
                && a.getValue(FACING) == b.getValue(FACING);
    }

    private Predicate<BlockState> sameStructure(BlockState sample) {
        return s -> sameWindow(s, sample);
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
            if (!level.isClientSide() && !WallPlanePlacement.suppressSiblingBreak()) {
                WallPlanePlacement.runSuppressingSiblings(
                        () -> level.destroyBlock(currentPos, false));
            }
            return level.getBlockState(currentPos);
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isMaster(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    private VoxelShape shapeFor(BlockState state) {
        StyledWindowSeriesSpec spec = spec();
        int s = Mth.clamp(state.getValue(SHAPE), 0, spec.shapeCount() - 1);
        int u = state.getValue(PART_U);
        int v = state.getValue(PART_V);
        VoxelShape north = spec.shapeNorthForPart(s, u, v);
        Direction dir = state.getValue(FACING);
        return switch (dir) {
            case NORTH, SOUTH, EAST, WEST -> VoxelShapeRotation.rotateYFromNorth(north, dir);
            default -> north;
        };
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BREAKING_PLAYER.set(player);
        if (!level.isClientSide() && !WallPlanePlacement.suppressSiblingBreak()) {
            WallPlanePlacement.runSuppressingSiblings(
                    () ->
                            WallPlanePlacement.destroySiblingsExcept(
                                    level,
                                    pos,
                                    state,
                                    spec().footprint(),
                                    state.getValue(FACING),
                                    state.getValue(PART_U),
                                    state.getValue(PART_V),
                                    sameStructure(state)));
            if (!WallPlanePlacement.isCreative(player)) {
                Block.popResource(level, pos, stackForShape(state, 0));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(
            BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        try {
            if (!state.is(newState.getBlock())
                    && !level.isClientSide()
                    && !WallPlanePlacement.suppressSiblingBreak()) {
                WallPlanePlacement.runSuppressingSiblings(
                        () ->
                                WallPlanePlacement.destroySiblingsExcept(
                                        level,
                                        pos,
                                        state,
                                        spec().footprint(),
                                        state.getValue(FACING),
                                        state.getValue(PART_U),
                                        state.getValue(PART_V),
                                        sameStructure(state)));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        } finally {
            BREAKING_PLAYER.remove();
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        int shape = Mth.clamp(state.getValue(SHAPE), 0, spec().shapeCount() - 1);
        return stackForShape(state, shape);
    }

    private ItemStack stackForShape(BlockState state, int shape) {
        ItemStack stack = new ItemStack(asItem());
        if (shape != 0) {
            stack.getOrCreateTag().putInt(seriesId.shapeNbtKey(), shape);
        }
        return stack;
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        BlockPos master = masterPos(state, pos);
        BlockState masterState = level.getBlockState(master);
        if (!(masterState.getBlock() instanceof StyledWindowSeriesBlock)) {
            return InteractionResult.PASS;
        }
        int s = spec().nextShapeInCycle(masterState.getValue(SHAPE));
        setShapeOnFootprint(level, master, masterState, s);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 整足迹同步造型。 */
    public static void setShapeOnFootprint(Level level, BlockPos masterPos, BlockState masterState, int shape) {
        if (!(masterState.getBlock() instanceof StyledWindowSeriesBlock block) || !isMaster(masterState)) {
            return;
        }
        WallPlaneFootprint fp = block.spec().footprint();
        Direction facing = masterState.getValue(FACING);
        fp.forEachCell(
                masterPos,
                facing,
                (u, v) -> {
                    BlockPos cell = fp.cellPos(masterPos, facing, u, v);
                    BlockState cur = level.getBlockState(cell);
                    if (cur.getBlock() instanceof StyledWindowSeriesBlock
                            && block.sameWindow(cur, masterState)) {
                        level.setBlock(
                                cell,
                                cur.setValue(SHAPE, shape),
                                Block.UPDATE_ALL_IMMEDIATE);
                    }
                });
    }

    /** 刷子：同系列下一色，整足迹替换（保 facing/shape/part）。 */
    public static BlockState nextColorState(BlockState state) {
        if (!(state.getBlock() instanceof StyledWindowSeriesBlock current)) {
            return state;
        }
        StyledWindowSeriesSpec spec = current.spec();
        int next = (current.colorIndex + 1) % spec.colorCount();
        Block nextBlock = StyledWindowSeriesRegistration.block(current.seriesId, next).get();
        return nextBlock
                .defaultBlockState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(SHAPE, state.getValue(SHAPE))
                .setValue(PART_U, state.getValue(PART_U))
                .setValue(PART_V, state.getValue(PART_V));
    }

    /** 服务端刷子：从任意格换整足迹颜色。 */
    public static boolean applyRecolorFootprint(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof StyledWindowSeriesBlock block)) {
            return false;
        }
        BlockPos master = masterPos(state, pos);
        BlockState masterState = level.getBlockState(master);
        if (!(masterState.getBlock() instanceof StyledWindowSeriesBlock)) {
            return false;
        }
        WallPlaneFootprint fp = block.spec().footprint();
        Direction facing = masterState.getValue(FACING);
        fp.forEachCell(
                master,
                facing,
                (u, v) -> {
                    BlockPos cell = fp.cellPos(master, facing, u, v);
                    BlockState cur = level.getBlockState(cell);
                    if (!(cur.getBlock() instanceof StyledWindowSeriesBlock)) {
                        return;
                    }
                    BlockState next = nextColorState(cur);
                    level.setBlock(cell, next, Block.UPDATE_ALL_IMMEDIATE);
                });
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
