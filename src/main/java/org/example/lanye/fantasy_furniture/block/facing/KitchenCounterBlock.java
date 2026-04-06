package org.example.lanye.fantasy_furniture.block.facing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.KitchenCounterBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 料理台（MoonStarfish Geo）：水平朝向；碰撞为整格。
 */
public class KitchenCounterBlock extends GeolibFacingEntityBlockWithFactory<KitchenCounterBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public KitchenCounterBlock(BlockBehaviour.Properties properties) {
        super(properties, KitchenCounterBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
