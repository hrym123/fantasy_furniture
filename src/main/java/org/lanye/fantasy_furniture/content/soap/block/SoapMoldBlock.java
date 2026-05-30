package org.lanye.fantasy_furniture.content.soap.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 肥皂模具：可放置 Geo 方块（{@code geo_collision_box.py --gecko-block} 北向盒）。 */
public class SoapMoldBlock extends GeolibFacingEntityBlockWithFactory<SoapMoldBlockEntity> {

    public static final VoxelShape SHAPE_NORTH = Block.box(1.86, 0.0, 7.0, 11.8, 4.0, 12.0);

    public SoapMoldBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapMoldBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(SHAPE_NORTH, state.getValue(FACING));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(new ItemStack(asItem()));
    }
}
