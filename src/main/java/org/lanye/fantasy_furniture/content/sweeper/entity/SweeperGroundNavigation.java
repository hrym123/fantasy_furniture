package org.lanye.fantasy_furniture.content.sweeper.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

/**
 * 扫地机器人专用地面导航器：先与原版行为保持一致，后续在此按需扩展 createPath 策略。
 */
public final class SweeperGroundNavigation extends GroundPathNavigation {
    public SweeperGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * 从当前实体位置到目标方块格求一条地面 {@link Path}，供 {@link SweeperRobotEntity} 等逻辑缓存/跟点使用。
     * <p>
     * 语义与 {@link net.minecraft.world.entity.ai.navigation.PathNavigation#createPath(BlockPos, int)} 一致：
     * {@code pPos} 为寻路终点格；{@code pAccuracy} 为调用方传入的容差参数（原版常用 0~2），本扫地机导航器固定向父类传入 0，
     * 避免因非零容差提前判达导致跟点行为异常。
     * 扫地机专用寻路策略（如候选终点）可在此扩展，容差仍可保持 0。
     *
     * @param pPos      目标方块坐标
     * @param pAccuracy 保留签名兼容；当前实现不向父类传入该值
     * @return 可行路径；若无路径或不可寻路则返回 {@code null}
     */
    @Override
    public Path createPath(BlockPos pPos, int pAccuracy) {
        return super.createPath(pPos, 0);
    }
}
