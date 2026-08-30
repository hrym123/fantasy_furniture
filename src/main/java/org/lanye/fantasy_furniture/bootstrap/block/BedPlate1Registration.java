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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1Materials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlockItem;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer;
import org.lanye.reverie_core.util.ReveriePerfRender;

/**
 * 床板1型三材质档：block id = {@code bed_plate1_<色>}；共用 geo {@code bed_plate1}。
 */
public final class BedPlate1Registration {

    private static final Map<BedPlate1MaterialVariant, RegistryObject<Block>> BLOCKS_BY_VARIANT =
            new EnumMap<>(BedPlate1MaterialVariant.class);

    private static final List<RegistryObject<Block>> BLOCKS = registerBlocks();

    private static final RegistryObject<BlockEntityType<BedPlate1BlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    BedPlate1Materials.GEO_STEM,
                    () ->
                            BlockEntityType.Builder.of(
                                            BedPlate1BlockEntity::new,
                                            BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    private static final AnimatedBlockEntry<BedPlateBaseBlockEntity> ENTRY =
            new AnimatedBlockEntry<>(
                    BLOCKS_BY_VARIANT.get(BedPlate1MaterialVariant.DEFAULT),
                    ITEMS.get(BedPlate1MaterialVariant.DEFAULT.ordinal()),
                    castBet(BLOCK_ENTITY_TYPE));

    private BedPlate1Registration() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static RegistryObject<BlockEntityType<BedPlateBaseBlockEntity>> castBet(
            RegistryObject<BlockEntityType<BedPlate1BlockEntity>> bet) {
        return (RegistryObject) bet;
    }

    private static List<RegistryObject<Block>> registerBlocks() {
        List<RegistryObject<Block>> list = new ArrayList<>();
        for (BedPlate1MaterialVariant v : BedPlate1MaterialVariant.VALUES) {
            final BedPlate1MaterialVariant vv = v;
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            vv.blockId(),
                            () ->
                                    new BedPlate1Block(
                                            FurnitureBlockProperties.cherryWoodFurnitureNoOcclusion(), vv));
            BLOCKS_BY_VARIANT.put(vv, ro);
            list.add(ro);
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (BedPlate1MaterialVariant v : BedPlate1MaterialVariant.VALUES) {
            final BedPlate1MaterialVariant vv = v;
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
                                                    BedPlate1Materials.GEO_STEM,
                                                    vv.textureStem()))));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<BedPlateBaseBlockEntity> entry() {
        return ENTRY;
    }

    public static RegistryObject<BlockEntityType<BedPlate1BlockEntity>> blockEntityType() {
        return BLOCK_ENTITY_TYPE;
    }

    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }

    public static List<RegistryObject<Block>> blocks() {
        return BLOCKS;
    }

    public static RegistryObject<Block> block(BedPlate1MaterialVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }

    public static void registerClientRenderers() {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ENTRY,
                ReveriePerfRender.wrapBer(
                        ctx ->
                                new BedPlateGeoBlockRenderer(
                                        FantasyFurniture.MODID,
                                        BedPlate1Materials.GEO_STEM,
                                        be -> ((BedPlate1BlockEntity) be).getTextureLocation())));
    }

    public static void bootstrap() {
        // no-op：触发 static 初始化
    }
}
