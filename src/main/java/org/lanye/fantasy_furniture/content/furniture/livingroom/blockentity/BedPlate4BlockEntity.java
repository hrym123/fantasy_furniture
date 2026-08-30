package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/** 床板4型：仅空床体（基础形态）。 */
public final class BedPlate4BlockEntity extends BedPlateBaseBlockEntity {

    public BedPlate4BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BED_PLATE4.blockEntityType().get(), pos, state);
    }
}
