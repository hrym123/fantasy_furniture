package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate3Registration;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3Materials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate3Block;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/** 床板3型：共用 BE，贴图随 {@link BedPlate3Block#variant()}。 */
public final class BedPlate3BlockEntity extends BedPlateBaseBlockEntity {

    public BedPlate3BlockEntity(BlockPos pos, BlockState state) {
        super(BedPlate3Registration.blockEntityType().get(), pos, state);
    }

    public ResourceLocation getTextureLocation() {
        Block block = getBlockState().getBlock();
        if (block instanceof BedPlate3Block plate) {
            return BedPlate3Materials.texture(plate.variant());
        }
        return BedPlate3Materials.texture(BedPlate3MaterialVariant.DEFAULT);
    }
}
