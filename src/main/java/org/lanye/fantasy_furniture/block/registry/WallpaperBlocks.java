package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 壁纸与同类墙饰：较软，侧面颜色决定 {@link MapColor}。
 */
public final class WallpaperBlocks {

    private WallpaperBlocks() {}

    static BlockBehaviour.Properties wallpaperProperties(MapColor sideColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(sideColor)
                .strength(0.6f)
                .sound(SoundType.WOOL);
    }

    public static final SimpleBlockRegistration.SimpleBlockEntry PINK_WALLPAPER =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "pink_wallpaper",
                    wallpaperProperties(MapColor.TERRACOTTA_PINK));

    public static final SimpleBlockRegistration.SimpleBlockEntry RED_WALLPAPER =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "red_wallpaper",
                    wallpaperProperties(MapColor.TERRACOTTA_RED));

    public static final SimpleBlockRegistration.SimpleBlockEntry YELLOW_WALLPAPER =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "yellow_wallpaper",
                    wallpaperProperties(MapColor.TERRACOTTA_YELLOW));

    /** 黄色墙裙：顶 / 侧 / 底分贴图（与壁纸同类属性，锄头可挖）。 */
    public static final SimpleBlockRegistration.SimpleBlockEntry YELLOW_WAINSCOT =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "yellow_wainscot",
                    wallpaperProperties(MapColor.TERRACOTTA_YELLOW));

    public static final SimpleBlockRegistration.SimpleBlockEntry BLUE_WALLPAPER =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "blue_wallpaper",
                    wallpaperProperties(MapColor.TERRACOTTA_BLUE));

    public static final SimpleBlockRegistration.SimpleBlockEntry GREEN_WALLPAPER =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "green_wallpaper",
                    wallpaperProperties(MapColor.TERRACOTTA_GREEN));

    public static final SimpleBlockRegistration.SimpleBlockEntry PURPLE_WALLPAPER =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "purple_wallpaper",
                    wallpaperProperties(MapColor.TERRACOTTA_PURPLE));
}
