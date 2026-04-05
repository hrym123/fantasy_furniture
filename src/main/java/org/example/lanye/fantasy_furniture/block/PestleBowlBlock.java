package org.example.lanye.fantasy_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.PestleBowlBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibEntityBlockWithFactory;

/**
 * 捣蒜碗：对方块交互一次即触发一次捣碎动画（{@link PestleBowlBlockEntity#onServerMash}）。
 * <p>
 * 碰撞与 {@code pestle_bowl.bbmodel} 全体元素包围盒一致（模型空间并集经 +8 平移至 0～16）。
 */
public class PestleBowlBlock extends GeolibEntityBlockWithFactory<PestleBowlBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 11.9, 13.0);

    public PestleBowlBlock(Properties properties) {
        super(properties, PestleBowlBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PestleBowlBlockEntity bowl) {
                bowl.onServerMash();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
