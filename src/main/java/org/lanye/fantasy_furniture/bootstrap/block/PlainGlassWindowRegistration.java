package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainGlassWindowBlockItem;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import net.minecraftforge.registries.RegistryObject;

/**
 * 普通玻璃窗：单方块 id + 按材质多套 {@link GeolibBlockItem}（id 后缀为 {@link PlainGlassWindowMaterialVariant} 颜色名，与
 * {@link PlainGlassWindowMaterials} 槽序一致）。
 */
public final class PlainGlassWindowRegistration {

    private static final ResourceLocation PLAIN_GLASS_WINDOW_PREVIEW_GEO =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/plain_glass_window_shape_straight.geo.json");

    private static final ResourceLocation GEOLIB_STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    private static final RegistryObject<Block> BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_glass_window",
                    () -> new PlainGlassWindowBlock(FurnitureBlockProperties.glassWindow()));

    private static final RegistryObject<BlockEntityType<PlainGlassWindowBlockEntity>> BLOCK_ENTITY_TYPE =
            ModBlockEntities.BLOCK_ENTITY_TYPES.register(
                    "plain_glass_window",
                    () ->
                            BlockEntityType.Builder.of(
                                            PlainGlassWindowBlockEntity::new, BLOCK.get())
                                    .build(null));

    private static final List<RegistryObject<Item>> ITEMS = registerItems();

    private static final AnimatedBlockEntry<PlainGlassWindowBlockEntity> ENTRY =
            new AnimatedBlockEntry<>(BLOCK, ITEMS.get(0), BLOCK_ENTITY_TYPE);

    private PlainGlassWindowRegistration() {}

    private static List<RegistryObject<Item>> registerItems() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (PlainGlassWindowMaterialVariant v : PlainGlassWindowMaterialVariant.values()) {
            final PlainGlassWindowMaterialVariant vv = v;
            int m = vv.ordinal();
            list.add(
                    ModBlocks.BLOCK_ITEMS.register(
                            "plain_glass_window_" + vv.getSerializedName(),
                            () ->
                                    new PlainGlassWindowBlockItem(
                                            BLOCK.get(),
                                            new Item.Properties(),
                                            new GeolibItemAssets(
                                                    PLAIN_GLASS_WINDOW_PREVIEW_GEO,
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            FantasyFurniture.MODID,
                                                            "textures/block/"
                                                                    + PlainGlassWindowMaterials.itemPreviewStem(m)
                                                                    + ".png"),
                                                    GEOLIB_STATIC_ANIMATION),
                                            vv)));
        }
        return List.copyOf(list);
    }

    public static AnimatedBlockEntry<PlainGlassWindowBlockEntity> entry() {
        return ENTRY;
    }

    /** 与 {@link PlainGlassWindowMaterials} 顺序一致。 */
    public static List<RegistryObject<Item>> items() {
        return ITEMS;
    }
}
