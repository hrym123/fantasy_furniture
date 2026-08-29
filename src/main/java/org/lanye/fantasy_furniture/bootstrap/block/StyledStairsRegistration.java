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
import org.lanye.fantasy_furniture.content.furniture.decor.StyledStairsMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledStairsMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.StyledStairsBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.StyledStairsBlockEntity;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 楼梯 21 材质档：block id = item id = {@code styled_stairs_<色>}；共用 geo {@code styled_stairs}。
 */
public final class StyledStairsRegistration {

    private static final String FURNITURE_STEM = "styled_stairs";
    private static final String GEO_STEM = StyledStairsMaterials.GEO_STEM;

    private static final Map<StyledStairsMaterialVariant, RegistryObject<Block>> BLOCKS_BY_VARIANT =
            new EnumMap<>(StyledStairsMaterialVariant.class);

    private static final List<RegistryObject<Block>> BLOCKS = registerBlocks();

    private static final RegistryObject<BlockEntityType<StyledStairsBlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    FURNITURE_STEM,
                    () ->
                            BlockEntityType.Builder.of(
                                            StyledStairsBlockEntity::new,
                                            BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    private static final AnimatedBlockEntry<StyledStairsBlockEntity> ENTRY =
            new AnimatedBlockEntry<>(
                    BLOCKS_BY_VARIANT.get(StyledStairsMaterialVariant.WHITE),
                    ITEMS.get(StyledStairsMaterialVariant.WHITE.ordinal()),
                    BLOCK_ENTITY_TYPE);

    private StyledStairsRegistration() {}

    private static List<RegistryObject<Block>> registerBlocks() {
        List<RegistryObject<Block>> list = new ArrayList<>();
        for (StyledStairsMaterialVariant v : StyledStairsMaterialVariant.VALUES) {
            final StyledStairsMaterialVariant vv = v;
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            vv.blockId(),
                            () ->
                                    new StyledStairsBlock(
                                            FurnitureBlockProperties.cherryWoodFurnitureNoOcclusion(), vv));
            BLOCKS_BY_VARIANT.put(vv, ro);
            list.add(ro);
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (StyledStairsMaterialVariant v : StyledStairsMaterialVariant.VALUES) {
            final StyledStairsMaterialVariant vv = v;
            RegistryObject<Block> blockRo = BLOCKS_BY_VARIANT.get(vv);
            list.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            vv.blockId(),
                            () ->
                                    new GeolibBlockItem(
                                            blockRo.get(),
                                            new Item.Properties(),
                                            GeolibItemAssets.blockAssetWithTexture(
                                                    FantasyFurniture.MODID, GEO_STEM, vv.textureStem()))));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<StyledStairsBlockEntity> entry() {
        return ENTRY;
    }

    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }

    public static List<RegistryObject<Block>> blocks() {
        return BLOCKS;
    }

    public static RegistryObject<Block> block(StyledStairsMaterialVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }

    /** 触发 static 初始化。 */
    public static void bootstrap() {
        // no-op
    }
}
