package org.lanye.fantasy_furniture.content.sweeper.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 扫地机器人专用地面导航器：先与原版行为保持一致，后续在此按需扩展 createPath 策略。
 */
public final class SweeperGroundNavigation extends GroundPathNavigation {
    public SweeperGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * 最简实现：直接构造“当前位置到目标坐标”的路径，仅保留起点+终点两个节点，不依赖父类寻路算法。
     * <p>
     * 掉落物的完整 {@link Vec3} 分量存入 {@link SweeperItemGroundPath}，供最后一档航点使用；{@link Node}
     * 仍为整数格索引（结构所需），不在此把目标坐标先收成 {@link BlockPos} 再参与“终点跟随”计算。
     */
    public Path createPathToExactPos(Vec3 targetPos) {
        if (this.mob.getY() < (double) this.level.getMinBuildHeight()) {
            return null;
        }

        Vec3 exact = new Vec3(targetPos.x, targetPos.y, targetPos.z);
        BlockPos start = this.mob.blockPosition();
        int gx = Mth.floor(targetPos.x);
        int gy = Mth.floor(targetPos.y);
        int gz = Mth.floor(targetPos.z);
        BlockPos goalBlock = new BlockPos(gx, gy, gz);

        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(start.getX(), gy, start.getZ()));
        if (!start.equals(goalBlock)) {
            nodes.add(new Node(gx, gy, gz));
        }
        return new SweeperItemGroundPath(nodes, goalBlock, true, exact);
    }

    /** 兼容原有调用：将方块格转换为中心点后走统一精确坐标入口。 */
    @Override
    public Path createPath(BlockPos pPos, int pAccuracy) {
        return createPathToExactPos(Vec3.atCenterOf(pPos));
    }
}
