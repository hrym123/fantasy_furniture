package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesBlockItem;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesCatalog;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesId;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesSpec;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 型号窗 1～7（含 1.5 / 2.5）REG-608 批量注册：每色 block=item 同 id；每型号一个 BET。
 */
public final class StyledWindowSeriesRegistration {

    private static final ResourceLocation GEOLIB_STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    private static final Map<StyledWindowSeriesId, List<RegistryObject<Block>>> BLOCKS =
            new EnumMap<>(StyledWindowSeriesId.class);
    private static final Map<StyledWindowSeriesId, List<RegistryObject<Item>>> ITEMS =
            new EnumMap<>(StyledWindowSeriesId.class);
    private static final Map<StyledWindowSeriesId, RegistryObject<BlockEntityType<StyledWindowSeriesBlockEntity>>>
            BETS = new EnumMap<>(StyledWindowSeriesId.class);
    private static final Map<StyledWindowSeriesId, AnimatedBlockEntry<StyledWindowSeriesBlockEntity>> ENTRIES =
            new EnumMap<>(StyledWindowSeriesId.class);
    private static final Map<String, RegistryObject<Block>> BLOCK_BY_FULL_ID = new HashMap<>();

    static {
        for (StyledWindowSeriesSpec spec : StyledWindowSeriesCatalog.all()) {
            registerSeries(spec);
        }
    }

    private StyledWindowSeriesRegistration() {}

    private static void registerSeries(StyledWindowSeriesSpec spec) {
        StyledWindowSeriesId id = spec.id();
        List<RegistryObject<Block>> blocks = new ArrayList<>();
        for (int c = 0; c < spec.colorCount(); c++) {
            final int color = c;
            String regName = id.idPrefix() + "_" + spec.colorId(c);
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            regName,
                            () ->
                                    new StyledWindowSeriesBlock(
                                            FurnitureBlockProperties.glassWindow(), id, color));
            blocks.add(ro);
            BLOCK_BY_FULL_ID.put(regName, ro);
        }
        BLOCKS.put(id, List.copyOf(blocks));

        RegistryObject<BlockEntityType<StyledWindowSeriesBlockEntity>> bet =
                ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                        id.blockEntityTypeId(),
                        () ->
                                BlockEntityType.Builder.of(
                                                (pos, state) ->
                                                        new StyledWindowSeriesBlockEntity(id, pos, state),
                                                blocks.stream()
                                                        .map(RegistryObject::get)
                                                        .toArray(Block[]::new))
                                        .build(null));
        BETS.put(id, bet);

        List<RegistryObject<Item>> items = new ArrayList<>();
        for (int c = 0; c < spec.colorCount(); c++) {
            final int color = c;
            String regName = id.idPrefix() + "_" + spec.colorId(c);
            RegistryObject<Block> blockRo = blocks.get(c);
            items.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            regName,
                            () ->
                                    new StyledWindowSeriesBlockItem(
                                            blockRo.get(),
                                            new Item.Properties(),
                                            new GeolibItemAssets(
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            FantasyFurniture.MODID, spec.previewGeoPath()),
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            FantasyFurniture.MODID,
                                                            "textures/block/"
                                                                    + spec.textureStem(color)
                                                                    + ".png"),
                                                    GEOLIB_STATIC_ANIMATION),
                                            id,
                                            color)));
        }
        ITEMS.put(id, List.copyOf(items));

        ENTRIES.put(
                id,
                new AnimatedBlockEntry<>(blocks.get(0), items.get(0), bet));
    }

    public static AnimatedBlockEntry<StyledWindowSeriesBlockEntity> entry(StyledWindowSeriesId id) {
        return ENTRIES.get(id);
    }

    public static RegistryObject<BlockEntityType<StyledWindowSeriesBlockEntity>> blockEntityType(
            StyledWindowSeriesId id) {
        return BETS.get(id);
    }

    public static RegistryObject<Block> block(StyledWindowSeriesId id, int colorIndex) {
        return BLOCKS.get(id).get(colorIndex);
    }

    public static List<RegistryObject<Block>> blocks(StyledWindowSeriesId id) {
        return BLOCKS.get(id);
    }

    public static List<RegistryObject<Item>> items(StyledWindowSeriesId id) {
        return ITEMS.get(id);
    }

    /** 创造栏：按系列声明序展开全部色。 */
    public static List<RegistryObject<Item>> allItemsInOrder() {
        List<RegistryObject<Item>> all = new ArrayList<>();
        for (StyledWindowSeriesId id : StyledWindowSeriesId.values()) {
            all.addAll(ITEMS.get(id));
        }
        return List.copyOf(all);
    }

    public static List<RegistryObject<Block>> allBlocksInOrder() {
        List<RegistryObject<Block>> all = new ArrayList<>();
        for (StyledWindowSeriesId id : StyledWindowSeriesId.values()) {
            all.addAll(BLOCKS.get(id));
        }
        return List.copyOf(all);
    }

    /** 触发 static 初始化（由 {@link FurnitureAnimatedBlocks} 引用）。 */
    public static void bootstrap() {
        // no-op
    }
}
