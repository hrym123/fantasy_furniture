package org.lanye.fantasy_furniture.content.soap.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;

public final class BodyWashBlockEntity extends SoapStaticGeoBlockEntity {

    public BodyWashBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BODY_WASH.blockEntityType().get(), pos, state);
    }
}
