package org.lanye.fantasy_furniture.content.furniture.bar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.bar.BarMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.bar.blockentity.CornerBarBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;

/** 转角吧台（MoonStarfish Geo）：水平朝向；碰撞为最小外接盒（整格）。 */
public class CornerBarBlock extends GeolibFacingEntityBlockWithFactory<CornerBarBlockEntity> {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    private final BarMaterialVariant variant;

    public CornerBarBlock(BlockBehaviour.Properties properties, BarMaterialVariant variant) {
        super(properties, CornerBarBlockEntity::new);
        this.variant = variant;
    }

    public BarMaterialVariant variant() {
        return variant;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
