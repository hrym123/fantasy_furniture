package org.lanye.fantasy_furniture.block.facing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.block.entity.KitchenCounterCabinetBlockEntity;
import org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 橱柜（MoonStarfish Geo）：水平朝向；碰撞为整格（模型与 {@code geo_collision_box.py} 外接盒一致）。
 */
public class KitchenCounterCabinetBlock extends GeolibFacingEntityBlockWithFactory<KitchenCounterCabinetBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public KitchenCounterCabinetBlock(BlockBehaviour.Properties properties) {
        super(properties, KitchenCounterCabinetBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
