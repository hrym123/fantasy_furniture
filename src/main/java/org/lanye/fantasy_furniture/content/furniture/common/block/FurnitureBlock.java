package org.lanye.fantasy_furniture.content.furniture.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.core.geolib.GeolibFacingEntityBlock;

/**
 * 家具方块交互基类：统一分流客户端/服务端的 {@link #use} 逻辑，避免各子类重复样板代码。
 */
public abstract class FurnitureBlock extends GeolibFacingEntityBlock {

    protected FurnitureBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public final InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return onUseClient(state, level, pos, player, hand, hit);
        }
        return onUseServer(state, level, pos, player, hand, hit);
    }

    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.SUCCESS;
    }

    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
