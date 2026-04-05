package org.example.lanye.fantasy_furniture.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.BanquetteBlockEntity;
import org.example.lanye.fantasy_furniture.block.state.BanquetteShape;
import org.example.lanye.fantasy_furniture.geolib.GeolibEntityBlockWithFactory;

/**
 * 卡座：GeckoLib 渲染；{@link BanquetteShape} 直段 / 拐角左拼 / 拐角右拼。
 * <p>
 * 前方邻格（{@code pos.relative(FACING)}）为卡座且其朝向为 {@link Direction#getClockWise()} 时为 {@link
 * BanquetteShape#CORNER_LEFT}；为 {@link Direction#getCounterClockWise()} 时为 {@link BanquetteShape#CORNER_RIGHT}；否则直段。
 * 例外：若本格<strong>左侧</strong>邻格已有与本座同向或「朝左」的卡座，则前方即使为朝左也不变拐角；右拼对称（<strong>右侧</strong>已有同向或朝右则不变右拐角）。
 * 客户端对右拼拐角在左拼基础上额外 Y 旋转 180°。
 */
public class BanquetteBlock extends GeolibEntityBlockWithFactory<BanquetteBlockEntity> {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
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
     * 拐角（左拼基准）：与 {@code banquette_corner.geo.json} 主要立方体对齐；右拼在旋转后再 {@link Rotation#CLOCKWISE_180}。
     */
    private static final VoxelShape CORNER_NORTH = orParts(
            Block.box(6, 4.9, 0.4, 16, 6.3, 10),
            Block.box(6, 0, 10, 16, 16, 16),
            Block.box(0, 0, 0, 6, 16, 16),
            Block.box(6, 0, 1, 16, 4.9, 10),
            Block.box(6, 0, 0, 15, 4.9, 1));

    public BanquetteBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
        super(properties, BanquetteBlockEntity::new);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SHAPE, BanquetteShape.STRAIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
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
     * 除非 {@link #shouldSuppressCornerLeft} / {@link #shouldSuppressCornerRight} 要求保持直段。
     */
    static BanquetteShape computeShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState ahead = level.getBlockState(pos.relative(facing));
        if (!isBanquette(ahead)) {
            return BanquetteShape.STRAIGHT;
        }
        Direction other = ahead.getValue(FACING);
        if (other == facing.getClockWise()) {
            if (shouldSuppressCornerLeft(level, pos, facing)) {
                return BanquetteShape.STRAIGHT;
            }
            return BanquetteShape.CORNER_LEFT;
        }
        if (other == facing.getCounterClockWise()) {
            if (shouldSuppressCornerRight(level, pos, facing)) {
                return BanquetteShape.STRAIGHT;
            }
            return BanquetteShape.CORNER_RIGHT;
        }
        return BanquetteShape.STRAIGHT;
    }

    /**
     * 本座左侧（{@code pos.relative(facing.getCounterClockWise())}）已有卡座，且其朝向为与本座相同或「朝左」
     *（{@code facing.getClockWise()}）时，不因前方朝左卡座而变为左拼拐角。
     */
    private static boolean shouldSuppressCornerLeft(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        if (!isBanquette(left)) {
            return false;
        }
        Direction lf = left.getValue(FACING);
        return lf == facing || lf == facing.getClockWise();
    }

    /**
     * 本座右侧（{@code pos.relative(facing.getClockWise())}）已有卡座，且其朝向为与本座相同或「朝右」
     *（{@code facing.getCounterClockWise()}）时，不因前方朝右卡座而变为右拼拐角。
     */
    private static boolean shouldSuppressCornerRight(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState right = level.getBlockState(pos.relative(facing.getClockWise()));
        if (!isBanquette(right)) {
            return false;
        }
        Direction rf = right.getValue(FACING);
        return rf == facing || rf == facing.getCounterClockWise();
    }

    private static boolean isBanquette(BlockState state) {
        return state.getBlock() instanceof BanquetteBlock;
    }

    private static VoxelShape orParts(VoxelShape first, VoxelShape... rest) {
        VoxelShape s = first;
        for (VoxelShape p : rest) {
            s = Shapes.or(s, p);
        }
        return s;
    }

    /**
     * 北向基准形状按水平 {@code facing} 绕 Y 轴旋转（与方块状态朝向一致）。
     * <p>
     * 使用 {@link VoxelShape#toAabbs()} 在 0～1 局部坐标下旋转各轴对齐盒再合并；勿用
     * {@link Block#rotate(BlockState, Rotation)}（仅适用于 {@link BlockState}）。
     */
    private static VoxelShape rotateFromNorth(VoxelShape northShape, Direction facing) {
        return rotateVoxelShape(northShape, rotationFromHorizontalNorth(facing));
    }

    private static Rotation rotationFromHorizontalNorth(Direction facing) {
        return switch (facing) {
            case NORTH -> Rotation.NONE;
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static VoxelShape rotateVoxelShape(VoxelShape shape, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return shape;
        }
        List<AABB> boxes = shape.toAabbs();
        VoxelShape out = Shapes.empty();
        for (AABB box : boxes) {
            AABB rotated =
                    switch (rotation) {
                        case CLOCKWISE_90 -> rotateAabbY90(box);
                        case CLOCKWISE_180 -> rotateAabbY180(box);
                        case COUNTERCLOCKWISE_90 -> rotateAabbY270(box);
                        default -> box;
                    };
            out = Shapes.or(out, aabbToShape(rotated));
        }
        return out;
    }

    /** {@link AABB} 为方块内 0～1 坐标，与 {@link Block#box} 的 0～16 一致。 */
    private static VoxelShape aabbToShape(AABB box) {
        return Block.box(
                box.minX * 16.0,
                box.minY * 16.0,
                box.minZ * 16.0,
                box.maxX * 16.0,
                box.maxY * 16.0,
                box.maxZ * 16.0);
    }

    /** 俯视顺时针 90°（与 {@link Rotation#CLOCKWISE_90} 一致）。 */
    private static AABB rotateAabbY90(AABB box) {
        double minX = box.minX;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxZ = box.maxZ;
        return new AABB(1.0 - maxZ, box.minY, minX, 1.0 - minZ, box.maxY, maxX);
    }

    private static AABB rotateAabbY180(AABB box) {
        return new AABB(1.0 - box.maxX, box.minY, 1.0 - box.maxZ, 1.0 - box.minX, box.maxY, 1.0 - box.minZ);
    }

    /** 俯视逆时针 90°，等价于连续三次顺时针 90°。 */
    private static AABB rotateAabbY270(AABB box) {
        double minX = box.minX;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxZ = box.maxZ;
        return new AABB(minZ, box.minY, 1.0 - maxX, maxZ, box.maxY, 1.0 - minX);
    }

    private static VoxelShape shapeFor(BlockState state) {
        Direction facing = state.getValue(FACING);
        return switch (state.getValue(SHAPE)) {
            case STRAIGHT -> rotateFromNorth(STRAIGHT_NORTH, facing);
            case CORNER_LEFT -> rotateFromNorth(CORNER_NORTH, facing);
            case CORNER_RIGHT -> rotateVoxelShape(rotateFromNorth(CORNER_NORTH, facing), Rotation.CLOCKWISE_180);
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
