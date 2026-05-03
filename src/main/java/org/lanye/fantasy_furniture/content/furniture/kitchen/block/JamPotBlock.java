package org.lanye.fantasy_furniture.content.furniture.kitchen.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.JamPotBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 果酱锅：仅在玩家（或实体）放置方块时播放一次入场动画（{@link #setPlacedBy}），右键对方块不触发动画。
 * <p>
 * 碰撞为北向基准（{@link #SHAPE_NORTH}）随 {@link org.lanye.reverie_core.geolib.GeolibFacingEntityBlock#FACING} 旋转；
 * 放置朝向见 {@link org.lanye.reverie_core.geolib.GeolibFacingEntityBlock}。
 */
public class JamPotBlock extends GeolibFacingEntityBlockWithFactory<JamPotBlockEntity> {

    /**
     * 与 Blockbench 模型全体元素的轴对齐包围盒一致（{@code jam_pot.bbmodel}，北向基准；
     * 模型空间 min(-6,0,-5) max(6,7.4,5)；水平平移 +8 与方块 0～16 对齐）。
     */
    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 0.0, 3.0, 14.0, 7.4, 13.0);

    public JamPotBlock(BlockBehaviour.Properties properties) {
        super(properties, JamPotBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof JamPotBlockEntity pot) {
                pot.onServerPlayEnter();
            }
        }
    }
}
