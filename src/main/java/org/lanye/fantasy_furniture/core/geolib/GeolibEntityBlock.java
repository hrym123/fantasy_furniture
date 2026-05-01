package org.lanye.fantasy_furniture.core.geolib;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 使用 {@link RenderShape#ENTITYBLOCK_ANIMATED} 的带实体方块基类，适用于 GeckoLib 方块实体渲染。
 * <p>
 * 子类实现 {@link #newBlockEntity} 即可；若构造方式固定，可对子类提供
 * {@link net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier} 构造器重载（见具体家具方块）。
 */
public abstract class GeolibEntityBlock extends BaseEntityBlock {

    protected GeolibEntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);
}
