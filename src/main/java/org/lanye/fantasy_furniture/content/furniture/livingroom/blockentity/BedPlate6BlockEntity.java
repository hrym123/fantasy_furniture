package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/**
 * 床板 6 的方块实体类型绑定（每款床板需单独 {@link net.minecraft.world.level.block.entity.BlockEntityType}，故保留薄子类）。
 */
public final class BedPlate6BlockEntity extends BedPlateBaseBlockEntity {

    public BedPlate6BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BED_PLATE6.blockEntityType().get(), pos, state);
    }
}
