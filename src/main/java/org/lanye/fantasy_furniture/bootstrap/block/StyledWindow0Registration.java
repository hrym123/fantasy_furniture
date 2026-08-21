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
import org.lanye.fantasy_furniture.content.furniture.common.state.StyledWindow0MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0Materials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.StyledWindow0Block;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.StyledWindow0BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.StyledWindow0BlockItem;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 0号窗户（REG-608）：每种颜色 {@code styled_window_0_<色>} 的 <strong>block id = item id</strong> 成对
 *（与 {@code styled_window_1} 等型号窗同前缀）；共用一个 {@link BlockEntityType}（注册名 {@code styled_window_0}）。
 */
public final class StyledWindow0Registration {

    private static final ResourceLocation STYLED_WINDOW_0_PREVIEW_GEO =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/styled_window_0_shape_straight.geo.json");

    private static final ResourceLocation GEOLIB_STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    private static final Map<StyledWindow0MaterialVariant, RegistryObject<Block>> BLOCKS_BY_VARIANT =
            new EnumMap<>(StyledWindow0MaterialVariant.class);

    private static final List<RegistryObject<Block>> BLOCKS = registerBlocks();

    private static final RegistryObject<BlockEntityType<StyledWindow0BlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    "styled_window_0",
                    () ->
                            BlockEntityType.Builder.of(
                                            StyledWindow0BlockEntity::new,
                                            BLOCKS.stream().map(RegistryObject::get).toArray(Block[]::new))
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    /** 白色条目：供 {@link ModBlocks#STYLED_WINDOW_0} / BER 使用（BET 已含全部色方块）。 */
    private static final AnimatedBlockEntry<StyledWindow0BlockEntity> ENTRY =
            new AnimatedBlockEntry<>(
                    BLOCKS_BY_VARIANT.get(StyledWindow0MaterialVariant.WHITE),
                    ITEMS.get(StyledWindow0MaterialVariant.WHITE.ordinal()),
                    BLOCK_ENTITY_TYPE);

    private StyledWindow0Registration() {}

    private static List<RegistryObject<Block>> registerBlocks() {
        List<RegistryObject<Block>> list = new ArrayList<>();
        for (StyledWindow0MaterialVariant v : StyledWindow0MaterialVariant.values()) {
            final StyledWindow0MaterialVariant vv = v;
            RegistryObject<Block> ro =
                    ModBlocks.BLOCKS.register(
                            "styled_window_0_" + vv.getSerializedName(),
                            () ->
                                    new StyledWindow0Block(
                                            FurnitureBlockProperties.glassWindow(), vv));
            BLOCKS_BY_VARIANT.put(vv, ro);
            list.add(ro);
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (StyledWindow0MaterialVariant v : StyledWindow0MaterialVariant.values()) {
            final StyledWindow0MaterialVariant vv = v;
            RegistryObject<Block> blockRo = BLOCKS_BY_VARIANT.get(vv);
            list.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            "styled_window_0_" + vv.getSerializedName(),
                            () ->
                                    new StyledWindow0BlockItem(
                                            blockRo.get(),
                                            new Item.Properties(),
                                            new GeolibItemAssets(
                                                    STYLED_WINDOW_0_PREVIEW_GEO,
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            FantasyFurniture.MODID,
                                                            "textures/block/"
                                                                    + StyledWindow0Materials.itemPreviewStem(
                                                                            vv.ordinal())
                                                                    + ".png"),
                                                    GEOLIB_STATIC_ANIMATION),
                                            vv)));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<StyledWindow0BlockEntity> entry() {
        return ENTRY;
    }

    public static RegistryObject<BlockEntityType<StyledWindow0BlockEntity>> blockEntityType() {
        return BLOCK_ENTITY_TYPE;
    }

    /** 与 {@link StyledWindow0Materials} / 枚举序一致。 */
    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }

    public static List<RegistryObject<Block>> blocks() {
        return BLOCKS;
    }

    public static RegistryObject<Block> block(StyledWindow0MaterialVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }
}
