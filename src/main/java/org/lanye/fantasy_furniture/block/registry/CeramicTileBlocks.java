package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 彩色瓷砖：仅 {@link MapColor} / 贴图路径不同，属性一致。
 */
public final class CeramicTileBlocks {

    private CeramicTileBlocks() {}

    static BlockBehaviour.Properties ceramicTileProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES);
    }

    public static final SimpleBlockRegistration.SimpleBlockEntry PINK_CERAMIC_TILE =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "pink_ceramic_tile",
                    ceramicTileProperties(MapColor.TERRACOTTA_PINK));

    public static final SimpleBlockRegistration.SimpleBlockEntry YELLOW_CERAMIC_TILE =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "yellow_ceramic_tile",
                    ceramicTileProperties(MapColor.TERRACOTTA_YELLOW));

    public static final SimpleBlockRegistration.SimpleBlockEntry BLUE_CERAMIC_TILE =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "blue_ceramic_tile",
                    ceramicTileProperties(MapColor.TERRACOTTA_BLUE));

    public static final SimpleBlockRegistration.SimpleBlockEntry GREEN_CERAMIC_TILE =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "green_ceramic_tile",
                    ceramicTileProperties(MapColor.TERRACOTTA_GREEN));

    public static final SimpleBlockRegistration.SimpleBlockEntry CYAN_CERAMIC_TILE =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "cyan_ceramic_tile",
                    ceramicTileProperties(MapColor.TERRACOTTA_CYAN));

    public static final SimpleBlockRegistration.SimpleBlockEntry PURPLE_CERAMIC_TILE =
            SimpleBlockRegistration.registerSimpleBlock(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    "purple_ceramic_tile",
                    ceramicTileProperties(MapColor.TERRACOTTA_PURPLE));
}
