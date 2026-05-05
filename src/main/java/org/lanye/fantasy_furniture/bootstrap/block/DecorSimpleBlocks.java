package org.lanye.fantasy_furniture.bootstrap.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.content.furniture.decor.block.DecorativeScreenBlock;

/**
 * 非纯色模板类简单方块：屏风等（非 {@link SimpleBlockRegistration} 的默认 {@link Block}）。
 *
 * <p><b>与 Gecko 家具链路的差异（排查问题时对照）：</b>{@link FurnitureAnimatedBlocks} 使用
 * {@link org.lanye.reverie_core.geolib.AnimatedBlockRegistration} + {@link org.lanye.reverie_core.geolib.GeolibBlockItem}
 * + 方块实体 + {@code geo/block/*.geo.json}；本类屏风为 <b>无方块实体</b> + {@link BlockItem} +
 * {@code models/block/*.json}。普通窗户见 {@link PlainWindowBlocks}（54 种 id，渲染层见 {@code ClientModEvents}）。
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
}
