package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibFurnitureRegistration;
import org.lanye.reverie_core.geolib.declarative.CatalogGeolibBlockEntity;
import org.lanye.reverie_core.geolib.declarative.GeolibBlockDefinition;
import org.lanye.reverie_core.geolib.declarative.RegisterGeolibBlock;

/**
 * 声明式 GeckoLib 家具 catalog：嵌套 {@link RegisterGeolibBlock} + {@link GeolibFurnitureRegistration} 共通链路。
 */
public final class FurnitureGeolibBlockCatalog {

    /** 须在 {@link #BOOTSTRAP} 之前完成初始化（bootstrap 扫描时会调用 {@link #propertiesForKey}）。 */
    private static final Map<String, Supplier<BlockBehaviour.Properties>> PROPERTIES =
            Map.ofEntries(
                    Map.entry("woodCabinetNoOcclusion", FurnitureBlockProperties::woodCabinetNoOcclusion),
                    Map.entry("metalNoOcclusion", FurnitureBlockProperties::metalNoOcclusion),
                    Map.entry(
                            "cherryWoodFurnitureNoOcclusion",
                            FurnitureBlockProperties::cherryWoodFurnitureNoOcclusion),
                    Map.entry(
                            "kitchenCeramicWhite",
                            () -> FurnitureBlockProperties.kitchenCeramic(MapColor.TERRACOTTA_WHITE)),
                    Map.entry(
                            "kitchenCeramicRed",
                            () -> FurnitureBlockProperties.kitchenCeramic(MapColor.COLOR_RED)));

    private static final GeolibFurnitureRegistration.DeclarativeCatalogBootstrap BOOTSTRAP =
            GeolibFurnitureRegistration.bootstrapDeclarativeCatalog(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    FantasyFurniture.MODID,
                    FurnitureGeolibBlockCatalog.class,
                    FurnitureGeolibBlockCatalog::propertiesForKey);

    private FurnitureGeolibBlockCatalog() {}

    public static Map<String, AnimatedBlockEntry<CatalogGeolibBlockEntity>> entries() {
        return BOOTSTRAP.entries();
    }

    public static Map<String, GeolibBlockDefinition> definitions() {
        return BOOTSTRAP.definitions();
    }

    public static java.util.List<Supplier<? extends ItemLike>> creativeTabItems() {
        return BOOTSTRAP.creativeTabItems();
    }

    public static AnimatedBlockEntry<CatalogGeolibBlockEntity> require(String id) {
        AnimatedBlockEntry<CatalogGeolibBlockEntity> entry = BOOTSTRAP.entries().get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown catalog block id: " + id);
        }
        return entry;
    }

    public static final AnimatedBlockEntry<CatalogGeolibBlockEntity> SPRUCE_TABLE = require("spruce_table");

    @RegisterGeolibBlock(
            id = "spruce_table",
            propertiesKey = "cherryWoodFurnitureNoOcclusion",
            shapeMinX = 0,
            shapeMinY = 0,
            shapeMinZ = 0,
            shapeMaxX = 16,
            shapeMaxY = 16,
            shapeMaxZ = 16)
    public static final class SpruceTable {}

    static Supplier<BlockBehaviour.Properties> propertiesForKey(String key) {
        Supplier<BlockBehaviour.Properties> supplier = PROPERTIES.get(key);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown propertiesKey for catalog block: " + key);
        }
        return supplier;
    }
}
