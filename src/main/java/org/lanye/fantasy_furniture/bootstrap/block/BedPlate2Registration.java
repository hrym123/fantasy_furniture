package org.lanye.fantasy_furniture.bootstrap.block;

import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate2Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BedPlate2GeoBlockRenderer;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.AnimatedBlockRegistration;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlockItem;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.util.ReveriePerfRender;

/** 床板2型：主方块 + BER；寝具与床板6型共用物品。 */
public final class BedPlate2Registration {

    private static final AnimatedBlockEntry<BedPlateBaseBlockEntity> MAIN = registerMainBlock();

    private BedPlate2Registration() {}

    private static AnimatedBlockEntry<BedPlateBaseBlockEntity> registerMainBlock() {
        return AnimatedBlockRegistration.registerSpec(
                ModBlocks.BLOCKS,
                ModBlocks.BLOCK_ITEMS,
                ModBlockEntities.BLOCK_ENTITY_TYPES,
                AnimatedBlockRegistration.spec(
                        "bed_plate2",
                        FurnitureBlockProperties::cherryWoodFurnitureNoOcclusion,
                        p -> new BedPlate2Block(p, BedPlate2BlockEntity::new),
                        BedPlate2BlockEntity::new,
                        (block, itemProps) ->
                                new BedPlateBlockItem(
                                        block,
                                        itemProps,
                                        GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "bed_plate2"))));
    }

    public static AnimatedBlockEntry<BedPlateBaseBlockEntity> mainEntry() {
        return MAIN;
    }

    public static void registerClientRenderer() {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                MAIN,
                ReveriePerfRender.wrapBer(ctx -> new BedPlate2GeoBlockRenderer()));
    }
}
