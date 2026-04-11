package org.lanye.fantasy_furniture.geolib;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@link GeolibEntityBlock} 的便捷子类：用 {@link BlockEntityType.BlockEntitySupplier} 创建方块实体。
 * 需要水平四向且默认朝向玩家时，请用 {@link GeolibFacingEntityBlockWithFactory}。
 *
 * @param <BE> 方块实体类型
 */
public abstract class GeolibEntityBlockWithFactory<BE extends BlockEntity> extends GeolibEntityBlock {

    private final BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier;

    protected GeolibEntityBlockWithFactory(Properties properties, BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier) {
        super(properties);
        this.blockEntitySupplier = blockEntitySupplier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntitySupplier.create(pos, state);
    }
}
