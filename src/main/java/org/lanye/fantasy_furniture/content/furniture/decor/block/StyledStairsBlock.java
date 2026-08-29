package org.lanye.fantasy_furniture.content.furniture.decor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledStairsMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.StyledStairsBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 楼梯（MoonStarfish Geo）：水平四向；碰撞为 geo cube 并集（可踩升）。
 */
public class StyledStairsBlock extends GeolibFacingEntityBlockWithFactory<StyledStairsBlockEntity> {

    /** 北向：geo_collision_box --gecko-block --emit-java（styled_stairs.geo.json）。 */
    private static final VoxelShape SHAPE_NORTH = buildShapeNorthUnion();

    private static final VoxelShape SHAPE_EAST = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH =
            VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.WEST);

    private final StyledStairsMaterialVariant variant;

    public StyledStairsBlock(BlockBehaviour.Properties properties, StyledStairsMaterialVariant variant) {
        super(properties, StyledStairsBlockEntity::new);
        this.variant = variant;
    }

    public StyledStairsMaterialVariant variant() {
        return variant;
    }

    private static VoxelShape buildShapeNorthUnion() {
        VoxelShape s = Shapes.empty();
        s = Shapes.or(s, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 8.0D));
        s = Shapes.or(s, Block.box(0.0D, 14.0D, 7.0D, 16.0D, 16.0D, 16.0D));
        s = Shapes.or(s, Block.box(0.0D, 0.0D, 8.0D, 16.0D, 14.0D, 16.0D));
        s = Shapes.or(s, Block.box(0.0D, 6.0D, 0.0D, 16.0D, 8.0D, 8.0D));
        return s;
    }

    private static VoxelShape shapeFor(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }
}
