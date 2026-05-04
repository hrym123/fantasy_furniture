package org.lanye.fantasy_furniture.content.sweeper.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 扫地机寻路/贴墙相关的纯函数，供 {@link SweeperRobotEntity} 与单元测试共用（避免测试加载实体类触发注册表初始化）。
 */
public final class SweeperRobotPathHelpers {
    private SweeperRobotPathHelpers() {}

    /**
     * 与 {@link org.lanye.fantasy_furniture.content.sweeper.client.renderer.SweeperRobotRenderer} 相同的贴墙 ±90° 四元数（绕水平轴）。
     */
    public static Quaternionf wallClimbTiltQuaternion(Direction wall, float yawDegrees) {
        Quaternionf identity = new Quaternionf();
        if (wall == null || !wall.getAxis().isHorizontal()) {
            return identity;
        }
        float ax = -wall.getStepZ();
        float az = wall.getStepX();
        float yR = yawDegrees * Mth.DEG_TO_RAD;
        float lx = ax * Mth.cos(yR) + az * Mth.sin(yR);
        float lz = -ax * Mth.sin(yR) + az * Mth.cos(yR);
        float len = Mth.sqrt(lx * lx + lz * lz);
        if (len <= 1.0e-4f) {
            return identity;
        }
        lx /= len;
        lz /= len;
        Quaternionf qPlus = new Quaternionf().rotateAxis(Mth.HALF_PI, lx, 0.0f, lz);
        Quaternionf qMinus = new Quaternionf().rotateAxis(-Mth.HALF_PI, lx, 0.0f, lz);
        float cosY = Mth.cos(yR);
        float sinY = Mth.sin(yR);
        Vector3f downLocalPlus = new Vector3f(0.0f, -1.0f, 0.0f).rotate(qPlus);
        float downPlusWorldX = downLocalPlus.x * cosY - downLocalPlus.z * sinY;
        float downPlusWorldZ = downLocalPlus.x * sinY + downLocalPlus.z * cosY;
        float downDotPlus = downPlusWorldX * wall.getStepX() + downPlusWorldZ * wall.getStepZ();
        Vector3f downLocalMinus = new Vector3f(0.0f, -1.0f, 0.0f).rotate(qMinus);
        float downMinusWorldX = downLocalMinus.x * cosY - downLocalMinus.z * sinY;
        float downMinusWorldZ = downLocalMinus.x * sinY + downLocalMinus.z * cosY;
        float downDotMinus = downMinusWorldX * wall.getStepX() + downMinusWorldZ * wall.getStepZ();
        return downDotPlus >= downDotMinus ? qPlus : qMinus;
    }

    /**
     * 沿墙位移仅从正东(+X)、西、南(+Z)、北 四向择一：尽量与墙面法向 (nx,nz) 垂直，并与参考方向 (preferX,preferZ) 同向优先。
     */
    public static Vec3 cardinalWallSlideUnit(double nx, double nz, double preferX, double preferZ) {
        double pl = Math.sqrt(preferX * preferX + preferZ * preferZ);
        if (pl > 1.0e-9D) {
            preferX /= pl;
            preferZ /= pl;
        } else {
            preferX = 1.0D;
            preferZ = 0.0D;
        }
        int bestCx = 1;
        int bestCz = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            int cx = d[0];
            int cz = d[1];
            double perpPenalty = Math.abs(cx * nx + cz * nz);
            double align = cx * preferX + cz * preferZ;
            double score = align - perpPenalty * 2.75D;
            if (score > bestScore) {
                bestScore = score;
                bestCx = cx;
                bestCz = cz;
            }
        }
        return new Vec3(bestCx, 0.0D, bestCz);
    }

    public static Vec3 collectWaypointCenter(Node n) {
        return Vec3.atCenterOf(new BlockPos(n.x, n.y, n.z));
    }

    /** 将驱动目标映射到其所在方块列的几何中心 XZ，保留 {@code goal.y}。 */
    public static Vec3 xzSnapGoalToBlockCenter(Vec3 goal) {
        BlockPos bp = BlockPos.containing(goal.x, goal.y, goal.z);
        Vec3 c = Vec3.atCenterOf(bp);
        return new Vec3(c.x, goal.y, c.z);
    }

    /**
     * 收集路径航点：中间节点为方块中心；{@link SweeperItemGroundPath} 末节点为 {@link SweeperItemGroundPath#exactItemPosition()}。
     */
    public static Vec3 collectGroundPathWaypoint(Path path, int nodeIndex) {
        if (path instanceof SweeperItemGroundPath sip && nodeIndex == path.getNodeCount() - 1) {
            Vec3 p = sip.exactItemPosition();
            return new Vec3(p.x, p.y, p.z);
        }
        return collectWaypointCenter(path.getNode(nodeIndex));
    }
}
