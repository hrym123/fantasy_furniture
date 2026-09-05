package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3Materials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate3Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BedPlate3GeoBlockRenderer;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlockItem;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.util.ReveriePerfRender;

/**
 * 床板3型两材质档：block id = {@code bed_plate3_<色>}；共用 geo {@code bed_plate3}。
 */
public final class BedPlate3Registration {

    private static final Map<BedPlate3MaterialVariant, RegistryObject<Block>> BLOCKS_BY_VARIANT =
            new EnumMap<>(BedPlate3MaterialVariant.class);

    private static final List<RegistryObject<Block>> BLOCKS = registerBlocks();

    private static final RegistryObject<BlockEntityType<BedPlate3BlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    BedPlate3Materials.GEO_STEM,
                    () ->
                            BlockEntityType.Builder.of(
                                            BedPlate3BlockEntity::new,
                                            BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    private static final AnimatedBlockEntry<BedPlateBaseBlockEntity> ENTRY =
            new AnimatedBlockEntry<>(
                    BLOCKS_BY_VARIANT.get(BedPlate3MaterialVariant.DEFAULT),
                    ITEMS.get(BedPlate3MaterialVariant.DEFAULT.ordinal()),
                    castBet(BLOCK_ENTITY_TYPE));

    private BedPlate3Registration() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static RegistryObject<BlockEntityType<BedPlateBaseBlockEntity>> castBet(
            RegistryObject<BlockEntityType<BedPlate3BlockEntity>> bet) {
        return (RegistryObject) bet;
    }

    private static List<RegistryObject<Block>> registerBlocks() {
        List<RegistryObject<Block>> list = new ArrayList<>();
        for (BedPlate3MaterialVariant v : BedPlate3MaterialVariant.VALUES) {
            final BedPlate3MaterialVariant vv = v;
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            vv.blockId(),
                            () ->
                                    new BedPlate3Block(
                                            FurnitureBlockProperties.cherryWoodFurnitureNoOcclusion(), vv));
            BLOCKS_BY_VARIANT.put(vv, ro);
            list.add(ro);
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (BedPlate3MaterialVariant v : BedPlate3MaterialVariant.VALUES) {
            final BedPlate3MaterialVariant vv = v;
            RegistryObject<Block> blockRo = BLOCKS_BY_VARIANT.get(vv);
            list.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            vv.blockId(),
                            () ->
                                    new BedPlateBlockItem(
                                            blockRo.get(),
                                            new Item.Properties(),
                                            GeolibItemAssets.blockAssetWithTexture(
                                                    FantasyFurniture.MODID,
                                                    BedPlate3Materials.GEO_STEM,
                                                    vv.textureStem()))));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<BedPlateBaseBlockEntity> entry() {
        return ENTRY;
    }

    public static RegistryObject<BlockEntityType<BedPlate3BlockEntity>> blockEntityType() {
        return BLOCK_ENTITY_TYPE;
    }

    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }

    public static List<RegistryObject<Block>> blocks() {
        return BLOCKS;
    }

    public static RegistryObject<Block> block(BedPlate3MaterialVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }

    public static void registerClientRenderers() {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ENTRY, ReveriePerfRender.wrapBer(ctx -> new BedPlate3GeoBlockRenderer()));
    }

    public static void bootstrap() {
        // no-op
    }
}
