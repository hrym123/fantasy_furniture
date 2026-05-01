package org.lanye.fantasy_furniture.bootstrap.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.content.furniture.decor.block.DecorativeScreenBlock;

/**
 * 非纯色模板类简单方块：屏风、玻璃窗等（非 {@link SimpleBlockRegistration} 的默认 {@link Block}）。
 */
public final class DecorSimpleBlocks {

    private DecorSimpleBlocks() {}

    private static BlockBehaviour.Properties decorativeScreenProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f)
                .sound(SoundType.CHERRY_WOOD)
                .noOcclusion();
    }

    /** 屏风：{@link DecorativeScreenBlock}（两格高、四向，无方块实体）。 */
    public static final RegistryObject<Block> DECORATIVE_SCREEN_BLOCK =
            ModBlocks.BLOCKS.register("decorative_screen", () -> new DecorativeScreenBlock(decorativeScreenProperties()));

    public static final RegistryObject<Item> DECORATIVE_SCREEN_ITEM =
            ModBlocks.BLOCK_ITEMS.register("decorative_screen", () -> new BlockItem(DECORATIVE_SCREEN_BLOCK.get(), new Item.Properties()));

    /** 玻璃窗：与原版玻璃相近的透明整格方块（无方块实体）。 */
    public static final RegistryObject<Block> GLASS_WINDOW_BLOCK =
            ModBlocks.BLOCKS.register("glass_window", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS)));

    public static final RegistryObject<Item> GLASS_WINDOW_ITEM =
            ModBlocks.BLOCK_ITEMS.register("glass_window", () -> new BlockItem(GLASS_WINDOW_BLOCK.get(), new Item.Properties()));
}
