package org.example.lanye.fantasy_furniture.geolib;

import java.util.function.Supplier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * GeckoLib 动画方块一条龙的工厂：一次调用产出 {@link AnimatedBlockSpec}，注册时配合
 * {@link AnimatedBlockRegistration#registerSpec} 使用，无需再写仅包装用的 Block / BlockItem 子类。
 *
 * @see GeolibBlockItem
 * @see SimpleGeolibEntityBlock
 */
public final class GeolibAnimatedFactories {

    private GeolibAnimatedFactories() {}

    /**
     * 标准组合：{@link SimpleGeolibEntityBlock} + {@link GeolibBlockItem}，资源由 {@link GeolibItemAssets} 描述。
     */
    public static <BE extends BlockEntity> AnimatedBlockSpec<BE> spec(
            String id,
            Supplier<BlockBehaviour.Properties> properties,
            BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier,
            GeolibItemAssets itemAssets,
            VoxelShape shape,
            InteractionResult useResult) {
        return AnimatedBlockRegistration.spec(
                id,
                properties,
                p -> new SimpleGeolibEntityBlock<>(p, blockEntitySupplier, shape, useResult),
                blockEntitySupplier,
                (Block block, Item.Properties itemProps) -> new GeolibBlockItem(block, itemProps, itemAssets));
    }
}
