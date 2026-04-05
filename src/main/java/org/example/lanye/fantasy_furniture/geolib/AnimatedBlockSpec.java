package org.example.lanye.fantasy_furniture.geolib;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 描述一条「带方块实体的方块」在注册期需要的信息（不可变）。
 * <p>
 * 与 {@link AnimatedBlockRegistration} 配合：服务端可安全持有本类型（不含客户端渲染器引用）。
 *
 * @param <BE> 方块实体类型
 * @see org.example.lanye.fantasy_furniture.geolib.client.AnimatedBlockClientRegistration 客户端 BER 绑定
 */
public record AnimatedBlockSpec<BE extends BlockEntity>(
        String id,
        Supplier<BlockBehaviour.Properties> properties,
        Function<BlockBehaviour.Properties, ? extends Block> blockFactory,
        BlockEntityType.BlockEntitySupplier<BE> beFactory,
        BiFunction<Block, Item.Properties, Item> itemFactory) {}
