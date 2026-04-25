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
import org.lanye.fantasy_furniture.block.entity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 组合摆件：子模型差分（底座 Geo + 玩偶 Geo）；潜行右键切换底座，普通右键切换玩偶。
 */
public class CombinedOrnamentBlock extends GeolibFacingEntityBlockWithFactory<CombinedOrnamentBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public CombinedOrnamentBlock(BlockBehaviour.Properties properties) {
        super(properties, CombinedOrnamentBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CombinedOrnamentBlockEntity ornament)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            ornament.cycleBase();
        } else {
            ornament.cycleFigurine();
        }
        return InteractionResult.CONSUME;
    }
}
