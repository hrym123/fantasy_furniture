package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock.CollisionMode;

/**
 * 普通窗户：9 种材质 × 6 种造型 = 54 个方块 id，表驱动注册；几何与贴图仍共用六套母版模型（见资源里
 * {@code models/block/plain_window*.json}），变体包装见 {@code models/block/plain_window_<材质>_<造型>.json}。
 */
public final class PlainWindowBlocks {

    private PlainWindowBlocks() {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    /** 与 {@link #registerAll()} 顺序一致：先按造型（模型）、再按材质（创造栏与此一致）。 */
    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    /**
     * 须在 {@link ModBlocks} 静态初始化阶段调用一次（在其它 RegistryObject 解析之前完成
     * {@link net.minecraftforge.registries.DeferredRegister#register}）。
     */
    public static void registerAll() {
        if (!ENTRIES.isEmpty()) {
            return;
        }
        for (Shape s : Shape.values()) {
            for (Material m : Material.values()) {
                String id = blockId(m, s);
                RegistryObject<Block> block =
                        ModBlocks.BLOCKS.register(id, () -> new PlainWindowBlock(plainWindowProperties(), s.mode, s.northShape));
                RegistryObject<Item> item =
                        ModBlocks.BLOCK_ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
                ENTRIES.add(new Entry(m, s, block, item));
            }
        }
    }

    public static String blockId(Material material, Shape shape) {
        return "plain_window_" + material.id + "_" + shape.id;
    }

    private static BlockBehaviour.Properties plainWindowProperties() {
        return BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion();
    }

    /** 九种材质（注册 id 小写）；贴图管线可按材质维度扩展，当前包装模型仍指向既有母版纹理。 */
    public enum Material {
        OAK("oak"),
        SPRUCE("spruce"),
        BIRCH("birch"),
        JUNGLE("jungle"),
        ACACIA("acacia"),
        DARK_OAK("dark_oak"),
        MANGROVE("mangrove"),
        CHERRY("cherry"),
        BAMBOO("bamboo");

        public final String id;

        Material(String id) {
            this.id = id;
        }
    }

    /** 六种造型：碰撞与母版模型路径。 */
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
    }

    public record Entry(Material material, Shape shape, RegistryObject<Block> block, RegistryObject<Item> item) {}

    /** 母版方块模型资源路径（不含 {@code .json}），与 {@code assets/.../models/block/} 下文件名一致。 */
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
