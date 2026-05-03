package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.seat.SeatInteraction;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.common.state.BanquetteShape;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 卡座：GeckoLib 渲染；{@link BanquetteShape} 直段 / 拐角左拼 / 拐角右拼。
 * <p>
 * 前方邻格（{@code pos.relative(FACING)}）为卡座且其朝向为 {@link Direction#getClockWise()} 时为 {@link
 * BanquetteShape#CORNER_LEFT}；为 {@link Direction#getCounterClockWise()} 时为 {@link BanquetteShape#CORNER_RIGHT}；否则直段。
 * 例外：若本格<strong>左侧</strong>邻格已有与本座同向或「朝左」的卡座，则前方即使为朝左也不变拐角；右拼对称（<strong>右侧</strong>已有同向或朝右则不变右拐角）。
 * 右拼拐角渲染的附加 Y 旋转见 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BanquetteGeoBlockRenderer}；碰撞在
 * {@link #shapeFor} 中另行处理以对齐线框与模型。
 */
public class BanquetteBlock extends GeolibFacingEntityBlockWithFactory<BanquetteBlockEntity> {

    public static final EnumProperty<BanquetteShape> SHAPE = EnumProperty.create("shape", BanquetteShape.class);

    /**
     * 直段：与 {@code banquette_straight.geo.json} 主要立方体对齐（北向：靠背朝 -Z，坐垫朝 +Z）。
     */
    private static final VoxelShape STRAIGHT_NORTH = orParts(
            Block.box(0, 0, 10, 16, 16, 16),
            Block.box(0, 0, 1, 16, 5, 10),
            Block.box(0, 4.9, 0.4, 16, 6.3, 10),
            Block.box(0, 7.3, 9.7, 16, 14.3, 10));

    /**
     * 拐角（左拼基准）：与 {@code banquette_corner.geo.json} 主要立方体对齐；坐垫条带与直段同高（{@code y∈[4.9,6.3]}）：
     * 沿 +Z 臂为 {@code [6,4.9,0.4]–[16,6.3,10]}，沿另一臂为 {@code [0,4.9,0.4]–[6,6.3,10]}，靠内角 {@code z∈[0,0.4]} 一条
     * （对应 geo {@code [-2,4.9,-8]} 并补全至 {@code x=0}）。靠背软垫同直段对应拐角 geo。
     * 右拼碰撞：在 {@link Rotation#CLOCKWISE_180} 后再 {@link Rotation#CLOCKWISE_90}，与当前客户端渲染下拐角模型对齐（仅改碰撞旋转，渲染保持原样）。
     */
    private static final VoxelShape CORNER_NORTH = orParts(
            Block.box(6, 4.9, 0.4, 16, 6.3, 10),
            Block.box(0, 4.9, 0.4, 6, 6.3, 10),
            Block.box(0, 4.9, 0, 15.6, 6.3, 0.4),
            Block.box(6, 0, 10, 16, 16, 16),
            Block.box(0, 0, 0, 6, 16, 16),
            Block.box(6, 0, 1, 16, 4.9, 10),
            Block.box(6, 0, 0, 15, 4.9, 1),
            Block.box(6, 7.3, 9.7, 16, 14.3, 10),
            Block.box(6, 7.3, 0, 6.3, 14.3, 9.7));

    public BanquetteBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(properties, BanquetteBlockEntity::new);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SHAPE, BanquetteShape.STRAIGHT));
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            if (SeatInteraction.trySitFromBlockUse(sp, sl, pos, state)) {
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return state.setValue(SHAPE, computeShape(state, level, pos));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        refreshShape(level, pos, state);
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        refreshShape(level, pos, state);
    }

    private static void refreshShape(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        BanquetteShape next = computeShape(state, level, pos);
        if (next != state.getValue(SHAPE)) {
            level.setBlock(pos, state.setValue(SHAPE, next), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * 前方邻格为卡座：其朝向为 {@code facing.getClockWise()} → 左拼拐角；为 {@code getCounterClockWise()} → 右拼拐角，
     * 除非拐角抑制规则要求保持直段。实现见 {@link BanquetteShapeRules}。
     */
    static BanquetteShape computeShape(BlockState state, BlockGetter level, BlockPos pos) {
        return BanquetteShapeRules.computeShape(
                state, level, pos, s -> s.getBlock() instanceof BanquetteBlock);
    }

    private static VoxelShape orParts(VoxelShape first, VoxelShape... rest) {
        VoxelShape s = first;
        for (VoxelShape p : rest) {
            s = Shapes.or(s, p);
        }
        return s;
    }

    private static VoxelShape shapeFor(BlockState state) {
        Direction facing = state.getValue(FACING);
        return switch (state.getValue(SHAPE)) {
            case STRAIGHT -> VoxelShapeRotation.rotateYFromNorth(STRAIGHT_NORTH, facing);
            case CORNER_LEFT -> VoxelShapeRotation.rotateYFromNorth(CORNER_NORTH, facing);
            case CORNER_RIGHT ->
                    VoxelShapeRotation.rotate(
                            VoxelShapeRotation.rotate(
                                    VoxelShapeRotation.rotateYFromNorth(CORNER_NORTH, facing),
                                    Rotation.CLOCKWISE_180),
                            Rotation.CLOCKWISE_90);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
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
}
