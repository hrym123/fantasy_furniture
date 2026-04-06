package org.example.lanye.fantasy_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.LotteryMachineBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 抽奖机（MoonStarfish Geo）：右键播放抽奖动画。
 * <p>
 * 碰撞为外接轴对齐盒：脚本将几何水平限制在放置格内，竖直允许超过一格（y 上限可大于 16），以覆盖高出单格的模型；
 * 再随 {@link #FACING} 预旋转四向。镂空碰撞可用脚本的 {@code --emit-java}。
 */
public class LotteryMachineBlock extends GeolibFacingEntityBlockWithFactory<LotteryMachineBlockEntity> {

    /** 北向基准：与 {@code geo_collision_box.py} 默认输出一致（max y 可大于 16，延伸到上方格）。 */
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 3.0, 16.0, 19.47, 16.0);

    private static final VoxelShape SHAPE_EAST = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.WEST);

    public LotteryMachineBlock(BlockBehaviour.Properties properties) {
        super(properties, LotteryMachineBlockEntity::new);
    }

    private static VoxelShape computeShape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return computeShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return computeShape(state);
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LotteryMachineBlockEntity machine) {
                machine.onServerDraw();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
