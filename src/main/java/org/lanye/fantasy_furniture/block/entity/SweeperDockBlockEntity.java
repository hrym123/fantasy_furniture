package org.lanye.fantasy_furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 机仓方块实体：提供停靠位与附近容器检索能力。 */
public class SweeperDockBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SweeperDockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SWEEPER_DOCK.blockEntityType().get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** 查询机仓周围可存储容器。 */
    public Container findNearbyContainer() {
        Level level = getLevel();
        if (level == null) {
            return null;
        }
        BlockPos center = getBlockPos();
        for (BlockPos pos : BlockPos.withinManhattan(center, 1, 1, 1)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container) {
                return container;
            }
        }
        return null;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1.0);
    }

    public static BlockEntityType<SweeperDockBlockEntity> type() {
        return ModBlocks.SWEEPER_DOCK.blockEntityType().get();
    }
}
