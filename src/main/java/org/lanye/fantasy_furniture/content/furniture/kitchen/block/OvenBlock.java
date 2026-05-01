package org.lanye.fantasy_furniture.content.furniture.kitchen.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.OvenBlockEntity;
import org.lanye.fantasy_furniture.core.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 烤箱：GeckoLib 静态模型，水平四向；放置逻辑见 {@link org.lanye.fantasy_furniture.core.geolib.GeolibFacingEntityBlock}。
 * 碰撞与 {@link Shapes#block()} 一致（与 {@code oven.bbmodel} 全体元素包围盒为整格相符）。
 */
public class OvenBlock extends GeolibFacingEntityBlockWithFactory<OvenBlockEntity> {

    private static final VoxelShape SHAPE = Shapes.block();

    public OvenBlock(BlockBehaviour.Properties properties) {
        super(properties, OvenBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
