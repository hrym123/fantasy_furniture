package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 纯色 / 单贴图差异的简单整格方块：一次登记 {@link Block} 与同 id 的 {@link BlockItem}。
 */
public final class SimpleBlockRegistration {

    private SimpleBlockRegistration() {}

    public record SimpleBlockEntry(RegistryObject<Block> block, RegistryObject<Item> item) {}

    public static SimpleBlockEntry registerSimpleBlock(
            DeferredRegister<Block> blocks,
            DeferredRegister<Item> items,
            String id,
            BlockBehaviour.Properties properties) {
        RegistryObject<Block> block = blocks.register(id, () -> new Block(properties));
        RegistryObject<Item> item = items.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return new SimpleBlockEntry(block, item);
    }
}
