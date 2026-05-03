package org.lanye.fantasy_furniture.content.furniture.kitchen.block;

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
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.PestleBowlBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 捣蒜碗：对方块交互一次即触发一次捣碎动画（{@link PestleBowlBlockEntity#onServerMash}）。
 * <p>
 * 碰撞仅对应 Geo 中 {@code group2} 五个立方体并集：模型坐标下 x/z 加 8 得到方块内 0～16，y 与方块竖直一致；
 * 并集为 {@code [3,13)×[0,7)×[3,13)}。水平朝向见 {@link org.lanye.reverie_core.geolib.GeolibFacingEntityBlock}。
 */
public class PestleBowlBlock extends GeolibFacingEntityBlockWithFactory<PestleBowlBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 7.0, 13.0);

    public PestleBowlBlock(BlockBehaviour.Properties properties) {
        super(properties, PestleBowlBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PestleBowlBlockEntity bowl) {
            bowl.onServerMash();
        }
        return InteractionResult.CONSUME;
    }
}
