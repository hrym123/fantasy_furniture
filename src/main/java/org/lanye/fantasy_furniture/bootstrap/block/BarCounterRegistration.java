package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.content.furniture.bar.BarBlockProperties;
import org.lanye.fantasy_furniture.content.furniture.bar.BarMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.bar.block.BarCounterBlock;
import org.lanye.fantasy_furniture.content.furniture.bar.blockentity.BarCounterBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 吧台 18 材质档：共用 geo {@code bar_counter} + 共享贴图 {@code {色}_bar_{档}}。 */
public final class BarCounterRegistration {

    private static final String FURNITURE_STEM = "bar_counter";
    private static final String GEO_STEM = "bar_counter";

    private static final Map<BarMaterialVariant, RegistryObject<Block>> BLOCKS_BY_VARIANT =
            new EnumMap<>(BarMaterialVariant.class);

    private static final List<RegistryObject<Block>> BLOCKS = registerBlocks();

    private static final RegistryObject<BlockEntityType<BarCounterBlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    FURNITURE_STEM,
                    () ->
                            BlockEntityType.Builder.of(
                                            BarCounterBlockEntity::new,
                                            BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    private static final AnimatedBlockEntry<BarCounterBlockEntity> ENTRY =
            new AnimatedBlockEntry<>(
                    BLOCKS_BY_VARIANT.get(BarMaterialVariant.GREEN_BAR_3),
                    ITEMS.get(BarMaterialVariant.GREEN_BAR_3.ordinal()),
                    BLOCK_ENTITY_TYPE);

    private BarCounterRegistration() {}

    private static List<RegistryObject<Block>> registerBlocks() {
        List<RegistryObject<Block>> list = new ArrayList<>();
        for (BarMaterialVariant variant : BarMaterialVariant.VALUES) {
            final BarMaterialVariant v = variant;
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            v.blockIdPrefix(FURNITURE_STEM),
                            () -> new BarCounterBlock(BarBlockProperties.woodCabinetNoOcclusion(), v));
            BLOCKS_BY_VARIANT.put(v, ro);
            list.add(ro);
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (BarMaterialVariant variant : BarMaterialVariant.VALUES) {
            final BarMaterialVariant v = variant;
            RegistryObject<Block> blockRo = BLOCKS_BY_VARIANT.get(v);
            list.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            v.blockIdPrefix(FURNITURE_STEM),
                            () ->
                                    new GeolibBlockItem(
                                            blockRo.get(),
                                            new Item.Properties(),
                                            GeolibItemAssets.blockAssetWithTexture(
                                                    org.lanye.fantasy_furniture.FantasyFurniture.MODID,
                                                    GEO_STEM,
                                                    v.textureStem()))));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<BarCounterBlockEntity> entry() {
        return ENTRY;
    }

    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }

    public static List<RegistryObject<Block>> blocks() {
        return BLOCKS;
    }

    public static RegistryObject<Block> block(BarMaterialVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }

    /** 触发 static 初始化（由 {@link ModBlocks} 引用）。 */
    public static void bootstrap() {}
}
