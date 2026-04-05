package org.example.lanye.fantasy_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.OvenBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibEntityBlockWithFactory;

/**
 * 烤箱：GeckoLib 静态模型，碰撞与 {@link Shapes#block()} 一致（与 {@code oven.bbmodel} 全体元素包围盒为整格相符）。
 */
public class OvenBlock extends GeolibEntityBlockWithFactory<OvenBlockEntity> {

    private static final VoxelShape SHAPE = Shapes.block();

    public OvenBlock(Properties properties) {
        super(properties, OvenBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
