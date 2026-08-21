package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainGlassWindowBlockItem;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 0号窗户（REG-608）：每种颜色 {@code styled_window_0_<色>} 的 <strong>block id = item id</strong> 成对
 *（与 {@code styled_window_1} 等型号窗同前缀）；共用一个 {@link BlockEntityType}（注册名 {@code styled_window_0}）。
 */
public final class PlainGlassWindowRegistration {

    private static final ResourceLocation PLAIN_GLASS_WINDOW_PREVIEW_GEO =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/plain_glass_window_shape_straight.geo.json");

    private static final ResourceLocation GEOLIB_STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    private static final Map<PlainGlassWindowMaterialVariant, RegistryObject<Block>> BLOCKS_BY_VARIANT =
            new EnumMap<>(PlainGlassWindowMaterialVariant.class);

    private static final List<RegistryObject<Block>> BLOCKS = registerBlocks();

    private static final RegistryObject<BlockEntityType<PlainGlassWindowBlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    "styled_window_0",
                    () ->
                            BlockEntityType.Builder.of(
                                            PlainGlassWindowBlockEntity::new,
                                            BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    /** 白色条目：供 {@link ModBlocks#PLAIN_GLASS_WINDOW} / BER 使用（BET 已含全部色方块）。 */
    private static final AnimatedBlockEntry<PlainGlassWindowBlockEntity> ENTRY =
            new AnimatedBlockEntry<>(
                    BLOCKS_BY_VARIANT.get(PlainGlassWindowMaterialVariant.WHITE),
                    ITEMS.get(PlainGlassWindowMaterialVariant.WHITE.ordinal()),
                    BLOCK_ENTITY_TYPE);

    private PlainGlassWindowRegistration() {}

    private static List<RegistryObject<Block>> registerBlocks() {
        List<RegistryObject<Block>> list = new ArrayList<>();
        for (PlainGlassWindowMaterialVariant v : PlainGlassWindowMaterialVariant.values()) {
            final PlainGlassWindowMaterialVariant vv = v;
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            "styled_window_0_" + vv.getSerializedName(),
                            () ->
                                    new PlainGlassWindowBlock(
                                            FurnitureBlockProperties.glassWindow(), vv));
            BLOCKS_BY_VARIANT.put(vv, ro);
            list.add(ro);
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (PlainGlassWindowMaterialVariant v : PlainGlassWindowMaterialVariant.values()) {
            final PlainGlassWindowMaterialVariant vv = v;
            RegistryObject<Block> blockRo = BLOCKS_BY_VARIANT.get(vv);
            list.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            "styled_window_0_" + vv.getSerializedName(),
                            () ->
                                    new PlainGlassWindowBlockItem(
                                            blockRo.get(),
                                            new Item.Properties(),
                                            new GeolibItemAssets(
                                                    PLAIN_GLASS_WINDOW_PREVIEW_GEO,
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            FantasyFurniture.MODID,
                                                            "textures/block/"
                                                                    + PlainGlassWindowMaterials.itemPreviewStem(
                                                                            vv.ordinal())
                                                                    + ".png"),
                                                    GEOLIB_STATIC_ANIMATION),
                                            vv)));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<PlainGlassWindowBlockEntity> entry() {
        return ENTRY;
    }

    public static RegistryObject<BlockEntityType<PlainGlassWindowBlockEntity>> blockEntityType() {
        return BLOCK_ENTITY_TYPE;
    }

    /** 与 {@link PlainGlassWindowMaterials} / 枚举序一致。 */
    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }

    public static List<RegistryObject<Block>> blocks() {
        return BLOCKS;
    }

    public static RegistryObject<Block> block(PlainGlassWindowMaterialVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }
}
