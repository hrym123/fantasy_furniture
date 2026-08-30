package org.lanye.fantasy_furniture.bootstrap.block;

import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate4BlockEntity;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.AnimatedBlockRegistration;
import org.lanye.reverie_core.geolib.bed.BedPlateAnimatedSpecs;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer;
import org.lanye.reverie_core.util.ReveriePerfRender;

/**
 * 床板4型空床体（单材质）。床板1/3 见 {@link BedPlate1Registration} / {@link BedPlate3Registration}；
 * 床板2/6 已有完整实装。
 */
public final class BedPlateBasicRegistration {

    private static final AnimatedBlockEntry<BedPlateBaseBlockEntity> BED_PLATE4 = registerBedPlate4();

    private BedPlateBasicRegistration() {}

    private static AnimatedBlockEntry<BedPlateBaseBlockEntity> registerBedPlate4() {
        return AnimatedBlockRegistration.registerSpec(
                ModBlocks.BLOCKS,
                ModBlocks.BLOCK_ITEMS,
                ModBlockEntities.BLOCK_ENTITY_TYPES,
                BedPlateAnimatedSpecs.spec(
                        FantasyFurniture.MODID,
                        "bed_plate4",
                        FurnitureBlockProperties::cherryWoodFurnitureNoOcclusion,
                        BedPlate4BlockEntity::new));
    }

    public static AnimatedBlockEntry<BedPlateBaseBlockEntity> bedPlate4() {
        return BED_PLATE4;
    }

    public static void registerClientRenderers() {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                BED_PLATE4,
                ReveriePerfRender.wrapBer(
                        ctx -> new BedPlateGeoBlockRenderer(FantasyFurniture.MODID, "bed_plate4")));
    }
}
