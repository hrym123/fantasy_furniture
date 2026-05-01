package org.lanye.fantasy_furniture.core.geolib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 带方块实体的方块：注册表门面。按设计书约定顺序向 {@link DeferredRegister} 排队：
 * <ol>
 *   <li>本批所有 {@link Block}</li>
 *   <li>本批所有 {@link BlockEntityType}</li>
 *   <li>本批所有 {@link Item}</li>
 * </ol>
 * 避免在构建 {@link BlockEntityType} 时出现同批内对方块解析顺序的隐含依赖。
 * <p>
 * 模组主类中仍须保证：对 mod 事件总线先订阅 {@code BLOCKS}（及方块物品表），再订阅 {@code BLOCK_ENTITY_TYPES}，
 * 与 Forge 惯例一致。
 */
public final class AnimatedBlockRegistration {

    private AnimatedBlockRegistration() {}

    /**
     * 登记多条 spec，按上述三阶段顺序写入三个 {@link DeferredRegister}。
     */
    @SuppressWarnings("unchecked")
    public static List<AnimatedBlockEntry<?>> registerSpecs(
            DeferredRegister<Block> blocks,
            DeferredRegister<Item> blockItems,
            DeferredRegister<BlockEntityType<?>> blockEntityTypes,
            AnimatedBlockSpec<?>... specs) {
        if (specs.length == 0) {
            return List.of();
        }
        int n = specs.length;
        RegistryObject<Block>[] blockRefs = new RegistryObject[n];
        for (int i = 0; i < n; i++) {
            AnimatedBlockSpec<?> spec = specs[i];
            blockRefs[i] =
                    blocks.register(spec.id(), () -> spec.blockFactory().apply(spec.properties().get()));
        }

        RegistryObject<BlockEntityType<?>>[] betRefs = new RegistryObject[n];
        for (int i = 0; i < n; i++) {
            AnimatedBlockSpec<?> spec = specs[i];
            RegistryObject<Block> blockRo = blockRefs[i];
            betRefs[i] =
                    blockEntityTypes.register(
                            spec.id(),
                            () ->
                                    BlockEntityType.Builder.of(
                                                    (BlockEntityType.BlockEntitySupplier<BlockEntity>)
                                                            spec.beFactory(),
                                                    blockRo.get())
                                            .build(null));
        }

        RegistryObject<Item>[] itemRefs = new RegistryObject[n];
        for (int i = 0; i < n; i++) {
            AnimatedBlockSpec<?> spec = specs[i];
            RegistryObject<Block> blockRo = blockRefs[i];
            BiFunction<Block, Item.Properties, Item> itemFactory = spec.itemFactory();
            itemRefs[i] =
                    blockItems.register(
                            spec.id(), () -> itemFactory.apply(blockRo.get(), new Item.Properties()));
        }

        List<AnimatedBlockEntry<?>> entries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            @SuppressWarnings("unchecked")
            RegistryObject<BlockEntityType<BlockEntity>> betTyped =
                    (RegistryObject<BlockEntityType<BlockEntity>>) (Object) betRefs[i];
            entries.add(
                    new AnimatedBlockEntry<BlockEntity>(
                            blockRefs[i], itemRefs[i], betTyped));
        }
        return entries;
    }

    /**
     * 等价于 {@link #registerSpecs(DeferredRegister, DeferredRegister, DeferredRegister, AnimatedBlockSpec[])}
     * 传入单条 spec。
     */
    @SuppressWarnings("unchecked")
    public static <BE extends BlockEntity> AnimatedBlockEntry<BE> registerSpec(
            DeferredRegister<Block> blocks,
            DeferredRegister<Item> blockItems,
            DeferredRegister<BlockEntityType<?>> blockEntityTypes,
            AnimatedBlockSpec<BE> spec) {
        List<AnimatedBlockEntry<?>> list =
                registerSpecs(blocks, blockItems, blockEntityTypes, spec);
        return (AnimatedBlockEntry<BE>) list.get(0);
    }

    /**
     * 便捷构造 {@link AnimatedBlockSpec}（与原先分散参数等价）。
     */
    public static <BE extends BlockEntity> AnimatedBlockSpec<BE> spec(
            String id,
            Supplier<BlockBehaviour.Properties> propertiesSupplier,
            Function<BlockBehaviour.Properties, ? extends Block> blockFactory,
            BlockEntityType.BlockEntitySupplier<BE> beFactory,
            BiFunction<Block, Item.Properties, Item> itemFactory) {
        return new AnimatedBlockSpec<>(id, propertiesSupplier, blockFactory, beFactory, itemFactory);
    }

    /** 将多条 spec 以可变参数形式传入时的便捷包装。 */
    public static List<AnimatedBlockEntry<?>> registerSpecs(
            DeferredRegister<Block> blocks,
            DeferredRegister<Item> blockItems,
            DeferredRegister<BlockEntityType<?>> blockEntityTypes,
            List<AnimatedBlockSpec<?>> specList) {
        return registerSpecs(
                blocks, blockItems, blockEntityTypes, specList.toArray(new AnimatedBlockSpec<?>[0]));
    }
}
