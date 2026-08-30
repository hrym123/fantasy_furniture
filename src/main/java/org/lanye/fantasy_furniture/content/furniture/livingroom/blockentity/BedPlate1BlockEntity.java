package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate1Registration;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1Materials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/** 床板1型：共用 BE，贴图随 {@link BedPlate1Block#variant()}。 */
public final class BedPlate1BlockEntity extends BedPlateBaseBlockEntity {

    public BedPlate1BlockEntity(BlockPos pos, BlockState state) {
        super(BedPlate1Registration.blockEntityType().get(), pos, state);
    }

    public ResourceLocation getTextureLocation() {
        Block block = getBlockState().getBlock();
        if (block instanceof BedPlate1Block plate) {
            return BedPlate1Materials.texture(plate.variant());
        }
        return BedPlate1Materials.texture(BedPlate1MaterialVariant.DEFAULT);
    }
}
