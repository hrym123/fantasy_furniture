package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/** 床板3型：材质档绑定在方块上，共用 geo。 */
public final class BedPlate3Block extends BedPlateBlock {

    private final BedPlate3MaterialVariant variant;

    public BedPlate3Block(BlockBehaviour.Properties properties, BedPlate3MaterialVariant variant) {
        super(properties, BedPlate3BlockEntity::new);
        this.variant = variant;
    }

    public BedPlate3MaterialVariant variant() {
        return variant;
    }
}
