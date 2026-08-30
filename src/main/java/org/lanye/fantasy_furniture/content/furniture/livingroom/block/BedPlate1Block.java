package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/** 床板1型：材质档绑定在方块上，共用 geo。 */
public final class BedPlate1Block extends BedPlateBlock {

    private final BedPlate1MaterialVariant variant;

    public BedPlate1Block(BlockBehaviour.Properties properties, BedPlate1MaterialVariant variant) {
        super(properties, BedPlate1BlockEntity::new);
        this.variant = variant;
    }

    public BedPlate1MaterialVariant variant() {
        return variant;
    }
}
