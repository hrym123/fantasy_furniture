package org.lanye.fantasy_furniture.geolib;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@link GeolibFacingEntityBlock} + {@link BlockEntityType.BlockEntitySupplier}，与 {@link GeolibEntityBlockWithFactory}
 * 的关系等同于「仅朝向」与「无朝向」之分。
 *
 * @param <BE> 方块实体类型
 */
public abstract class GeolibFacingEntityBlockWithFactory<BE extends BlockEntity> extends GeolibFacingEntityBlock {

    private final BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier;

    protected GeolibFacingEntityBlockWithFactory(
            BlockBehaviour.Properties properties, BlockEntityType.BlockEntitySupplier<BE> blockEntitySupplier) {
        super(properties);
        this.blockEntitySupplier = blockEntitySupplier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntitySupplier.create(pos, state);
    }
}
