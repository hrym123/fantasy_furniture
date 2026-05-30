package org.lanye.fantasy_furniture.content.soap.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;

public final class ShampooBlockEntity extends SoapStaticGeoBlockEntity {

    public ShampooBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SHAMPOO.blockEntityType().get(), pos, state);
    }
}
