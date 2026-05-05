package org.lanye.fantasy_furniture.bootstrap.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.content.furniture.decor.block.DecorativeScreenBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock.CollisionMode;

/**
 * 非纯色模板类简单方块：屏风、普通窗户等（非 {@link SimpleBlockRegistration} 的默认 {@link Block}）。
 *
 * <p><b>与 Gecko 家具链路的差异（排查问题时对照）：</b>{@link FurnitureAnimatedBlocks} 使用
 * {@link org.lanye.reverie_core.geolib.AnimatedBlockRegistration} + {@link org.lanye.reverie_core.geolib.GeolibBlockItem}
 * + 方块实体 + {@code geo/block/*.geo.json}；本类屏风/窗户为 <b>无方块实体</b> + {@link BlockItem} +
 * {@code models/block/*.json}，物品栏依赖方块模型烘焙，客户端半透明层由
 * {@link org.lanye.reverie_core.client.BlockRenderLayers} 统一注册渲染层（见 {@code ClientModEvents}，窗户为 cutout）。
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

    private static BlockBehaviour.Properties plainWindowProperties() {
        return BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion();
    }

    /** 普通窗户（默认朝向模型）：贴墙薄板，水平四向。 */
    public static final RegistryObject<Block> PLAIN_WINDOW_BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_window",
                    () -> new PlainWindowBlock(
                            plainWindowProperties(), CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_PLAIN_NORTH));

    public static final RegistryObject<Item> PLAIN_WINDOW_ITEM =
            ModBlocks.BLOCK_ITEMS.register("plain_window", () -> new BlockItem(PLAIN_WINDOW_BLOCK.get(), new Item.Properties()));

    /** 与 MoonStarfish「普通窗户」各角度变体对应的方块（行为与 {@link PlainWindowBlock} 相同；模型/贴图由 {@code tools/export_moonstarfish_plain_windows.py} 从素材目录同步）。 */
    public static final RegistryObject<Block> PLAIN_WINDOW_Y180_BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_window_y180",
                    () -> new PlainWindowBlock(
                            plainWindowProperties(), CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y180_NORTH));
    public static final RegistryObject<Item> PLAIN_WINDOW_Y180_ITEM =
            ModBlocks.BLOCK_ITEMS.register("plain_window_y180", () -> new BlockItem(PLAIN_WINDOW_Y180_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PLAIN_WINDOW_Y22_5_BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_window_y22_5",
                    () -> new PlainWindowBlock(
                            plainWindowProperties(), CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y22_5_NORTH));
    public static final RegistryObject<Item> PLAIN_WINDOW_Y22_5_ITEM =
            ModBlocks.BLOCK_ITEMS.register("plain_window_y22_5", () -> new BlockItem(PLAIN_WINDOW_Y22_5_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PLAIN_WINDOW_Y45_BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_window_y45",
                    () -> new PlainWindowBlock(
                            plainWindowProperties(), CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y45_NORTH));
    public static final RegistryObject<Item> PLAIN_WINDOW_Y45_ITEM =
            ModBlocks.BLOCK_ITEMS.register("plain_window_y45", () -> new BlockItem(PLAIN_WINDOW_Y45_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PLAIN_WINDOW_Y67_5_BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_window_y67_5",
                    () -> new PlainWindowBlock(
                            plainWindowProperties(), CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y67_5_NORTH));
    public static final RegistryObject<Item> PLAIN_WINDOW_Y67_5_ITEM =
            ModBlocks.BLOCK_ITEMS.register("plain_window_y67_5", () -> new BlockItem(PLAIN_WINDOW_Y67_5_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PLAIN_WINDOW_DIAGONAL_BLOCK =
            ModBlocks.BLOCKS.register(
                    "plain_window_diagonal",
                    () -> new PlainWindowBlock(plainWindowProperties(), CollisionMode.FULL_BLOCK, Shapes.block()));
    public static final RegistryObject<Item> PLAIN_WINDOW_DIAGONAL_ITEM =
            ModBlocks.BLOCK_ITEMS.register("plain_window_diagonal", () -> new BlockItem(PLAIN_WINDOW_DIAGONAL_BLOCK.get(), new Item.Properties()));
}
