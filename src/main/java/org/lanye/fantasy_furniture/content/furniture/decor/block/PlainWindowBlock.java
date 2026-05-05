package org.lanye.fantasy_furniture.content.furniture.decor.block;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.bootstrap.block.PlainWindowBlocks;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 贴墙「普通窗户」方块：水平四向，与 {@link DecorativeScreenBlock} 的朝向/旋转约定一致；无方块实体。
 *
 * <p>各变体北向基准碰撞盒由 {@code tools/geo_collision_box.py --mc-block-model} 根据
 * {@code assets/fantasy_furniture/models/block/plain_window*.json} 外接 AABB 生成，再经
 * {@link VoxelShapeRotation#rotateYFromNorth} 对齐 {@link #FACING}。
 *
 * <p>存活：优先邻格 {@link #hasHorizontalWallSupport} 贴墙；若无水平坚固面但正下方方块顶面坚固，则允许以「立在地面上」方式放置（竖直点击顶面常见）。
 */
public class PlainWindowBlock extends HorizontalDirectionalBlock {

    /**
     * {@code plain_window.json}：外接盒与模型一致（约 z∈[0,1.4]）。
     */
    public static final VoxelShape SHAPE_PLAIN_NORTH = Block.box(0, 0, 0, 16, 16, 1.4);

    /**
     * {@code plain_window_y180.json}。
     */
    public static final VoxelShape SHAPE_Y180_NORTH = Block.box(0, 0, 0, 16, 1.4, 16);

    /**
     * {@code plain_window_y22_5.json}（max y 大于 16 时碰撞向上格延伸）。
     */
    public static final VoxelShape SHAPE_Y22_5_NORTH = Block.box(0, 0, 0.0457, 16, 16.2127, 7.962);

    /** {@code plain_window_y45.json}。 */
    public static final VoxelShape SHAPE_Y45_NORTH = Block.box(0, 0, 0, 16, 16.1927, 16);

    /** {@code plain_window_y67_5.json}。 */
    public static final VoxelShape SHAPE_Y67_5_NORTH = Block.box(0, 0, 0.0704, 16, 7.1748, 16);

    public enum CollisionMode {

        /** 使用 {@link #northRefShape} + 水平旋转（默认窗与各角度变体）。 */
        ROTATED_FROM_NORTH,

        /** 整格固体（对角变体脚本外接盒为整格，与 {@link Shapes#block()} 等价）。 */
        FULL_BLOCK
    }

    private final CollisionMode collisionMode;
    private final VoxelShape northRefShape;
    private final String materialId;
    private final String shapeId;

    public PlainWindowBlocks.Material material() {
        return PlainWindowBlocks.Material.fromId(materialId);
    }

    public PlainWindowBlocks.Shape shape() {
        return PlainWindowBlocks.Shape.fromId(shapeId);
    }

    public PlainWindowBlock(
            BlockBehaviour.Properties properties,
            CollisionMode collisionMode,
            VoxelShape northRefShape,
            String materialId,
            String shapeId) {
        super(properties);
        this.collisionMode = collisionMode;
        this.northRefShape = northRefShape;
        this.materialId = materialId;
        this.shapeId = shapeId;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return PlainWindowBlocks.createStack(PlainWindowBlocks.Material.fromId(materialId), PlainWindowBlocks.Shape.fromId(shapeId));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        PlainWindowBlocks.Material mat = PlainWindowBlocks.Material.fromId(materialId);
        PlainWindowBlocks.Shape cur = PlainWindowBlocks.Shape.fromId(shapeId);
        PlainWindowBlocks.Shape[] all = PlainWindowBlocks.Shape.values();
        PlainWindowBlocks.Shape next = all[(cur.ordinal() + 1) % all.length];
        Direction facing = state.getValue(FACING);
        BlockState placed = PlainWindowBlocks.blockFor(mat, next).defaultBlockState().setValue(FACING, facing);
        if (!level.setBlock(pos, placed, Block.UPDATE_ALL)) {
            return InteractionResult.PASS;
        }
        level.playSound(null, pos, SoundEvents.GLASS_STEP, SoundSource.BLOCKS, 0.35F, 1.1F);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 与卡座等一致：getClickedPos() 已是目标放置格（勿再 relative(clickedFace)）。
        BlockPos pos = context.getClickedPos();
        Direction clicked = context.getClickedFace();

        if (clicked.getAxis().isHorizontal()) {
            BlockState state = defaultBlockState().setValue(FACING, clicked.getOpposite());
            boolean ok = state.canSurvive(context.getLevel(), pos);
            return ok ? state : null;
        }

        Set<Direction> tryOrder = new LinkedHashSet<>();
        tryOrder.add(context.getHorizontalDirection().getOpposite());
        for (Direction d : Direction.Plane.HORIZONTAL) {
            tryOrder.add(d);
        }

        LevelReader level = context.getLevel();
        for (Direction d : tryOrder) {
            BlockState state = defaultBlockState().setValue(FACING, d);
            if (hasHorizontalWallSupport(level, pos, d)) {
                return state;
            }
        }

        if (hasFloorSupport(level, pos)) {
            Direction d = context.getHorizontalDirection().getOpposite();
            return defaultBlockState().setValue(FACING, d);
        }

        return null;
    }

    private static boolean hasHorizontalWallSupport(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos wallPos = pos.relative(facing);
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, facing.getOpposite());
    }

    private static boolean hasFloorSupport(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction attach = state.getValue(FACING);
        BlockPos wallPos = pos.relative(attach);
        var wall = level.getBlockState(wallPos);
        boolean wallOk = wall.isFaceSturdy(level, wallPos, attach.getOpposite());
        boolean floorOk = hasFloorSupport(level, pos);
        return wallOk || floorOk;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(
            BlockState state,
            Direction facing,
            BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    private VoxelShape shapeFor(BlockState state) {
        if (collisionMode == CollisionMode.FULL_BLOCK) {
            return Shapes.block();
        }
        return VoxelShapeRotation.rotateYFromNorth(northRefShape, state.getValue(FACING));
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
        return PushReaction.DESTROY;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
