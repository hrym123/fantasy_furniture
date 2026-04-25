package org.lanye.fantasy_furniture.block.facing;

import net.minecraft.core.BlockPos;
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
import org.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 搅拌碗：对方块交互一次（原版 {@link net.minecraft.world.level.block.Block#use}）即由服务端触发一次搅拌动画。
 * 水平朝向见 {@link org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock}。
 */
public class MixingBowlBlock extends GeolibFacingEntityBlockWithFactory<MixingBowlBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 8, 14);

    public MixingBowlBlock(BlockBehaviour.Properties properties) {
        super(properties, MixingBowlBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MixingBowlBlockEntity bowl) {
            bowl.onServerShortStir();
        }
        return InteractionResult.CONSUME;
    }
}
