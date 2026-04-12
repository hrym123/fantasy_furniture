package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 壁纸与同类墙饰：较软，侧面颜色决定 {@link MapColor}。具体变种见 {@link WallpaperVariant}（表驱动注册）。
 */
public final class WallpaperBlocks {

    private WallpaperBlocks() {}

    static BlockBehaviour.Properties wallpaperProperties(MapColor sideColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(sideColor)
                .strength(0.6f)
                .sound(SoundType.WOOL);
    }

    private static SimpleBlockRegistration.SimpleBlockEntry wallpaper(String id, MapColor sideColor) {
        return SimpleBlockRegistration.registerSimpleBlock(
                ModBlocks.BLOCKS, ModBlocks.BLOCK_ITEMS, id, wallpaperProperties(sideColor));
    }

    /**
     * 壁纸 / 墙裙变种：枚举声明即「id + 地图色」表，构造时完成成对注册。
     * <p>黄色墙裙 {@link #YELLOW_WAINSCOT} 顶 / 侧 / 底分贴图（与壁纸同类属性，锄头可挖）。
     */
    public enum WallpaperVariant {
        PINK_WALLPAPER("pink_wallpaper", MapColor.TERRACOTTA_PINK),
        RED_WALLPAPER("red_wallpaper", MapColor.TERRACOTTA_RED),
        YELLOW_WALLPAPER("yellow_wallpaper", MapColor.TERRACOTTA_YELLOW),
        YELLOW_WAINSCOT("yellow_wainscot", MapColor.TERRACOTTA_YELLOW),
        BLUE_WALLPAPER("blue_wallpaper", MapColor.TERRACOTTA_BLUE),
        GREEN_WALLPAPER("green_wallpaper", MapColor.TERRACOTTA_GREEN),
        PURPLE_WALLPAPER("purple_wallpaper", MapColor.TERRACOTTA_PURPLE);

        private final SimpleBlockRegistration.SimpleBlockEntry entry;

        WallpaperVariant(String id, MapColor sideColor) {
            this.entry = wallpaper(id, sideColor);
        }

        public SimpleBlockRegistration.SimpleBlockEntry entry() {
            return entry;
        }
    }
}
