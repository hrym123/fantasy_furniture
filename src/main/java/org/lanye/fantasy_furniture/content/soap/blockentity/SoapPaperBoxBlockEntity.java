package org.lanye.fantasy_furniture.content.soap.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;

public final class SoapPaperBoxBlockEntity extends SoapStaticGeoBlockEntity {

    public SoapPaperBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOAP_PAPER_BOX.blockEntityType().get(), pos, state);
    }
}
