package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock.CollisionMode;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainWindowMaterialItem;

/**
 * 普通窗户：9 种颜色材质 × 6 种造型 = 54 个方块 id；创造栏仅 9 个材质物品（{@link PlainWindowMaterialItem}）；对已放置的方块潜行右键循环造型。
 */
public final class PlainWindowBlocks {

    private PlainWindowBlocks() {}

    private static final List<BlockEntry> BLOCK_ENTRIES = new ArrayList<>();
    private static final List<MaterialItemEntry> MATERIAL_ITEM_ENTRIES = new ArrayList<>();
    private static final Map<String, RegistryObject<Block>> BLOCK_BY_KEY = new HashMap<>();
    private static final Map<Material, RegistryObject<Item>> ITEM_BY_MATERIAL = new EnumMap<>(Material.class);

    /** 方块注册顺序：先造型再材质（与数据生成脚本一致）。 */
    public static List<BlockEntry> blockEntries() {
        return Collections.unmodifiableList(BLOCK_ENTRIES);
    }

    /** 创造栏用：每种材质一个物品。 */
    public static List<MaterialItemEntry> materialItemEntries() {
        return Collections.unmodifiableList(MATERIAL_ITEM_ENTRIES);
    }

    public static void registerAll() {
        if (!BLOCK_ENTRIES.isEmpty()) {
            return;
        }
        for (Shape s : Shape.values()) {
            for (Material m : Material.values()) {
                String id = blockId(m, s);
                String key = blockKey(m, s);
                RegistryObject<Block> block =
                        ModBlocks.BLOCKS.register(id, () -> new PlainWindowBlock(plainWindowProperties(), s.mode, s.northShape, m.id, s.id));
                BLOCK_BY_KEY.put(key, block);
                BLOCK_ENTRIES.add(new BlockEntry(m, s, block));
            }
        }
        for (Material m : Material.values()) {
            String itemId = materialItemId(m);
            RegistryObject<Item> item = ModBlocks.BLOCK_ITEMS.register(
                    itemId,
                    () -> new PlainWindowMaterialItem(blockFor(m, Shape.DEFAULT), m, new Item.Properties()));
            ITEM_BY_MATERIAL.put(m, item);
            MATERIAL_ITEM_ENTRIES.add(new MaterialItemEntry(m, item));
        }
    }

    private static String blockKey(Material m, Shape s) {
        return m.id + ":" + s.id;
    }

    public static String blockId(Material material, Shape shape) {
        return "plain_window_" + material.id + "_" + shape.id;
    }

    /** 与方块注册 id 不同的物品 id（无造型后缀）。 */
    public static String materialItemId(Material material) {
        return "plain_window_" + material.id;
    }

    public static Block blockFor(Material material, Shape shape) {
        RegistryObject<Block> ro = BLOCK_BY_KEY.get(blockKey(material, shape));
        if (ro == null) {
            throw new IllegalStateException("Missing plain window block: " + material + " " + shape);
        }
        return ro.get();
    }

    public static RegistryObject<Item> itemFor(Material material) {
        RegistryObject<Item> ro = ITEM_BY_MATERIAL.get(material);
        if (ro == null) {
            throw new IllegalStateException("Missing plain window item: " + material);
        }
        return ro;
    }

    public static ItemStack createStack(Material material, Shape shape) {
        return PlainWindowMaterialItem.createStack(material, shape);
    }

    private static BlockBehaviour.Properties plainWindowProperties() {
        return BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion();
    }

    public enum Material {
        WHITE("white"),
        CREAM("cream"),
        ROSE("rose"),
        MINT("mint"),
        SKY("sky"),
        LAVENDER("lavender"),
        PEACH("peach"),
        COCOA("cocoa"),
        SILVER("silver");

        public final String id;

        Material(String id) {
            this.id = id;
        }

        public static Material fromId(String id) {
            for (Material m : values()) {
                if (m.id.equals(id)) {
                    return m;
                }
            }
            return WHITE;
        }
    }

    public enum Shape {
        DEFAULT("default", CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_PLAIN_NORTH),
        Y180("y180", CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y180_NORTH),
        Y22_5("y22_5", CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y22_5_NORTH),
        Y45("y45", CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y45_NORTH),
        Y67_5("y67_5", CollisionMode.ROTATED_FROM_NORTH, PlainWindowBlock.SHAPE_Y67_5_NORTH),
        DIAGONAL("diagonal", CollisionMode.FULL_BLOCK, Shapes.block());

        public final String id;
        public final CollisionMode mode;
        public final VoxelShape northShape;

        Shape(String id, CollisionMode mode, VoxelShape northShape) {
            this.id = id;
            this.mode = mode;
            this.northShape = northShape;
        }

        public static Shape fromId(String id) {
            for (Shape s : values()) {
                if (s.id.equals(id)) {
                    return s;
                }
            }
            return DEFAULT;
        }
    }

    public record BlockEntry(Material material, Shape shape, RegistryObject<Block> block) {}

    public record MaterialItemEntry(Material material, RegistryObject<Item> item) {}

    public static String geometryModelPath(Shape shape) {
        return switch (shape) {
            case DEFAULT -> "plain_window";
            case Y180 -> "plain_window_y180";
            case Y22_5 -> "plain_window_y22_5";
            case Y45 -> "plain_window_y45";
            case Y67_5 -> "plain_window_y67_5";
            case DIAGONAL -> "plain_window_diagonal";
        };
    }
}
