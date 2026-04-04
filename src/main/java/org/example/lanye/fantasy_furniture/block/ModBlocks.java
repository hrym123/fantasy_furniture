package org.example.lanye.fantasy_furniture.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;

/**
 * 本模组方块及对应 {@link BlockItem} 的注册与引用。
 */
public final class ModBlocks {

    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Fantasy_furniture.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Fantasy_furniture.MODID);

    private static BlockBehaviour.Properties ceramicTileProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES);
    }

    /** 壁纸：较软，侧面颜色决定 {@link MapColor} */
    private static BlockBehaviour.Properties wallpaperProperties(MapColor sideColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(sideColor)
                .strength(0.6f)
                .sound(SoundType.WOOL);
    }

    /** 粉色瓷砖（材质见 {@code textures/block/pink_ceramic_tile.png}） */
    public static final RegistryObject<Block> PINK_CERAMIC_TILE_BLOCK = BLOCKS.register("pink_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_PINK)));
    public static final RegistryObject<Item> PINK_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("pink_ceramic_tile", () -> new BlockItem(PINK_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> YELLOW_CERAMIC_TILE_BLOCK = BLOCKS.register("yellow_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_YELLOW)));
    public static final RegistryObject<Item> YELLOW_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("yellow_ceramic_tile", () -> new BlockItem(YELLOW_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLUE_CERAMIC_TILE_BLOCK = BLOCKS.register("blue_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_BLUE)));
    public static final RegistryObject<Item> BLUE_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("blue_ceramic_tile", () -> new BlockItem(BLUE_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> GREEN_CERAMIC_TILE_BLOCK = BLOCKS.register("green_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_GREEN)));
    public static final RegistryObject<Item> GREEN_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("green_ceramic_tile", () -> new BlockItem(GREEN_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> CYAN_CERAMIC_TILE_BLOCK = BLOCKS.register("cyan_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_CYAN)));
    public static final RegistryObject<Item> CYAN_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("cyan_ceramic_tile", () -> new BlockItem(CYAN_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PURPLE_CERAMIC_TILE_BLOCK = BLOCKS.register("purple_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_PURPLE)));
    public static final RegistryObject<Item> PURPLE_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("purple_ceramic_tile", () -> new BlockItem(PURPLE_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PINK_WALLPAPER_BLOCK = BLOCKS.register("pink_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_PINK)));
    public static final RegistryObject<Item> PINK_WALLPAPER_ITEM = BLOCK_ITEMS.register("pink_wallpaper", () -> new BlockItem(PINK_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> RED_WALLPAPER_BLOCK = BLOCKS.register("red_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_RED)));
    public static final RegistryObject<Item> RED_WALLPAPER_ITEM = BLOCK_ITEMS.register("red_wallpaper", () -> new BlockItem(RED_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> YELLOW_WALLPAPER_BLOCK = BLOCKS.register("yellow_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_YELLOW)));
    public static final RegistryObject<Item> YELLOW_WALLPAPER_ITEM = BLOCK_ITEMS.register("yellow_wallpaper", () -> new BlockItem(YELLOW_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLUE_WALLPAPER_BLOCK = BLOCKS.register("blue_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_BLUE)));
    public static final RegistryObject<Item> BLUE_WALLPAPER_ITEM = BLOCK_ITEMS.register("blue_wallpaper", () -> new BlockItem(BLUE_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> GREEN_WALLPAPER_BLOCK = BLOCKS.register("green_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_GREEN)));
    public static final RegistryObject<Item> GREEN_WALLPAPER_ITEM = BLOCK_ITEMS.register("green_wallpaper", () -> new BlockItem(GREEN_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PURPLE_WALLPAPER_BLOCK = BLOCKS.register("purple_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_PURPLE)));
    public static final RegistryObject<Item> PURPLE_WALLPAPER_ITEM = BLOCK_ITEMS.register("purple_wallpaper", () -> new BlockItem(PURPLE_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
