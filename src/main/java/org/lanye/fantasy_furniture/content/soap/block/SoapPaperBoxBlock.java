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
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 包装盒：可放置 Geo 方块（套盒 / 摞放玩法待实装）。 */
public class SoapPaperBoxBlock extends GeolibFacingEntityBlockWithFactory<SoapPaperBoxBlockEntity> {

    public static final VoxelShape SHAPE_NORTH = Block.box(4.5, 0.0, 6.0, 11.5, 2.2, 10.0);

    public SoapPaperBoxBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapPaperBoxBlockEntity::new);
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
