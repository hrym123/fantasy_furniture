package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 彩色瓷砖：仅 {@link MapColor} / 贴图路径不同，属性一致。具体变种见 {@link TileVariant}（表驱动注册）。
 */
public final class CeramicTileBlocks {

    private CeramicTileBlocks() {}

    static BlockBehaviour.Properties ceramicTileProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES);
    }

    private static SimpleBlockRegistration.SimpleBlockEntry tile(String id, MapColor mapColor) {
        return SimpleBlockRegistration.registerSimpleBlock(
                ModBlocks.BLOCKS, ModBlocks.BLOCK_ITEMS, id, ceramicTileProperties(mapColor));
    }

    /**
     * 瓷砖颜色变种：枚举声明即「id + 地图色」表，构造时完成成对注册。
     */
    public enum TileVariant {
        PINK_CERAMIC_TILE("pink_ceramic_tile", MapColor.TERRACOTTA_PINK),
        YELLOW_CERAMIC_TILE("yellow_ceramic_tile", MapColor.TERRACOTTA_YELLOW),
        BLUE_CERAMIC_TILE("blue_ceramic_tile", MapColor.TERRACOTTA_BLUE),
        GREEN_CERAMIC_TILE("green_ceramic_tile", MapColor.TERRACOTTA_GREEN),
        CYAN_CERAMIC_TILE("cyan_ceramic_tile", MapColor.TERRACOTTA_CYAN),
        PURPLE_CERAMIC_TILE("purple_ceramic_tile", MapColor.TERRACOTTA_PURPLE);

        private final SimpleBlockRegistration.SimpleBlockEntry entry;

        TileVariant(String id, MapColor mapColor) {
            this.entry = tile(id, mapColor);
        }

        public SimpleBlockRegistration.SimpleBlockEntry entry() {
            return entry;
        }
    }
}
