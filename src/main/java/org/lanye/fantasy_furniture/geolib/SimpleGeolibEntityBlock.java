package org.lanye.fantasy_furniture.geolib;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 仅需要「GeckoLib 渲染 + 固定碰撞箱 + 固定交互结果」时的具体方块类，避免为每个家具再继承
 * {@link GeolibEntityBlockWithFactory}。
 */
public final class SimpleGeolibEntityBlock<BE extends BlockEntity> extends GeolibEntityBlockWithFactory<BE> {

    private final VoxelShape shape;
    private final InteractionResult useResult;

    public SimpleGeolibEntityBlock(
            Properties properties,
            BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier,
            VoxelShape shape,
            InteractionResult useResult) {
        super(properties, blockEntitySupplier);
        this.shape = shape;
        this.useResult = useResult;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return useResult;
    }
}
