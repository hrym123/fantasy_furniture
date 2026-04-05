package org.example.lanye.fantasy_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.HalfHalfPotBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 半半锅：MoonStarfish Geo；水平朝向见 {@link org.example.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock}；
 * 碰撞与模型大致对齐。
 */
public class HalfHalfPotBlock extends GeolibFacingEntityBlockWithFactory<HalfHalfPotBlockEntity> {

    /** 与 {@code half_half_pot.geo.json} 整体范围大致一致（像素坐标换算 0～16）。 */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 11.0, 15.0);

    public HalfHalfPotBlock(BlockBehaviour.Properties properties) {
        super(properties, HalfHalfPotBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
