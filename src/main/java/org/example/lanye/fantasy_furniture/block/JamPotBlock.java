package org.example.lanye.fantasy_furniture.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.JamPotBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibEntityBlockWithFactory;

/**
 * 果酱锅：仅在玩家（或实体）放置方块时播放一次入场动画（{@link #setPlacedBy}），右键对方块不触发动画。
 */
public class JamPotBlock extends GeolibEntityBlockWithFactory<JamPotBlockEntity> {

    /**
     * 与 Blockbench 模型全体元素的轴对齐包围盒一致（{@code jam_pot.bbmodel} 中 {@code elements} 的 from/to 并集：
     * 模型空间 min(-6,0,-5) max(6,7.4,5)；水平方向平移 +8 与方块 0～16 坐标对齐）。
     * <p>
     * 碰撞与轮廓均沿用 {@link Block#getCollisionShape} 对 {@link #getShape} 的默认关系。
     */
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 3.0, 14.0, 7.4, 13.0);

    public JamPotBlock(Properties properties) {
        super(properties, JamPotBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
