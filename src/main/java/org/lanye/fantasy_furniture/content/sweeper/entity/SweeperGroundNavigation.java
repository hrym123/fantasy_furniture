package org.lanye.fantasy_furniture.content.sweeper.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 扫地机器人专用地面导航器：收集路径的求路 **委托原版** {@link GroundPathNavigation#createPath(BlockPos, int)}（与
 * 其它地面生物相同的节点评估与 WalkNodeProcessor），再将 {@link Path} 包装为 {@link SweeperItemGroundPath}，以便实体侧仍用
 * 路径终点方块中心作为末档驱动目标。
 */
public final class SweeperGroundNavigation extends GroundPathNavigation {

    public SweeperGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * 以目标坐标所在格调用原版 {@link #createPath(BlockPos, int)}（accuracy 固定为 0，与
     * {@link SweeperRobotEntity#createCollectPathWithCollisionRadius} 当前用法一致），成功则返回
     * {@link SweeperItemGroundPath}；原版无路时返回 {@code null}（不再使用自定义格点 A* 与弦线兜底）。
     */
    public Path createPathToExactPos(Vec3 targetPos) {
        if (this.mob.getY() < (double) this.level.getMinBuildHeight()) {
            return null;
        }
        int gx = Mth.floor(targetPos.x);
        int gy = Mth.floor(targetPos.y);
        int gz = Mth.floor(targetPos.z);
        BlockPos goalBlock = new BlockPos(gx, gy, gz);
        Path vanilla = super.createPath(goalBlock, 0);
        return wrapVanillaItemPath(vanilla);
    }

    @Nullable
    private Path wrapVanillaItemPath(@Nullable Path vanilla) {
        if (vanilla == null || vanilla.getNodeCount() == 0) {
            return null;
        }
        BlockPos target = vanilla.getTarget();
        Vec3 terminal = Vec3.atCenterOf(target);
        List<Node> nodes = new ArrayList<>(vanilla.getNodeCount());
        for (int i = 0; i < vanilla.getNodeCount(); i++) {
            nodes.add(vanilla.getNode(i));
        }
        return new SweeperItemGroundPath(nodes, target, vanilla.canReach(), terminal);
    }

    @Override
    public Path createPath(BlockPos pPos, int pAccuracy) {
        Path vanilla = super.createPath(pPos, pAccuracy);
        return wrapVanillaItemPath(vanilla);
    }

    /** Chebyshev 距离；包级可见供单元测试（历史 A* 辅助，保留以稳定测试套件）。 */
    static int chebHeuristic(BlockPos a, BlockPos b) {
        return Math.max(
                Math.abs(a.getX() - b.getX()),
                Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }

    /**
     * 弦线兜底第二节点：在已解析的脚底候选中选距起点 Chebyshev 最近者；包级可见供单元测试。
     */
    @Nullable
    static BlockPos pickChordFootNearestToStart(
            BlockPos start,
            @Nullable BlockPos geomF,
            @Nullable BlockPos relaxedF,
            @Nullable BlockPos coarseF) {
        BlockPos best = null;
        int bestH = Integer.MAX_VALUE;
        int bestTierPri = -1;
        BlockPos[] cand = new BlockPos[] {geomF, relaxedF, coarseF};
        int[] tierPri = new int[] {2, 1, 0};
        for (int i = 0; i < 3; i++) {
            BlockPos c = cand[i];
            if (c == null) {
                continue;
            }
            int h = chebHeuristic(start, c);
            int pri = tierPri[i];
            if (h < bestH || (h == bestH && pri > bestTierPri)) {
                bestH = h;
                bestTierPri = pri;
                best = c;
            }
        }
        return best;
    }

    /** 历史收集格点邻接枚举；包级可见供单元测试。 */
    static List<BlockPos> collectGridNeighbors(BlockPos p) {
        List<BlockPos> out = new ArrayList<>(22);
        out.add(p.north());
        out.add(p.south());
        out.add(p.east());
        out.add(p.west());
        out.add(p.above());
        out.add(p.below());
        int y = p.getY();
        int x = p.getX();
        int z = p.getZ();
        out.add(new BlockPos(x + 1, y, z + 1));
        out.add(new BlockPos(x + 1, y, z - 1));
        out.add(new BlockPos(x - 1, y, z + 1));
        out.add(new BlockPos(x - 1, y, z - 1));
        out.add(new BlockPos(x + 1, y + 1, z));
        out.add(new BlockPos(x - 1, y + 1, z));
        out.add(new BlockPos(x + 1, y - 1, z));
        out.add(new BlockPos(x - 1, y - 1, z));
        out.add(new BlockPos(x, y + 1, z + 1));
        out.add(new BlockPos(x, y + 1, z - 1));
        out.add(new BlockPos(x, y - 1, z + 1));
        out.add(new BlockPos(x, y - 1, z - 1));
        return out;
    }

    /** 由 parent 指针链重建节点序列；包级可见供单元测试。 */
    static List<Node> buildNodeChain(Map<BlockPos, BlockPos> parent, BlockPos start, BlockPos goal) {
        List<BlockPos> backwards = new ArrayList<>();
        BlockPos at = goal;
        while (!at.equals(start)) {
            backwards.add(at);
            BlockPos prev = parent.get(at);
            if (prev == null || prev.equals(at)) {
                return null;
            }
            at = prev;
        }
        backwards.add(start);
        Collections.reverse(backwards);
        List<Node> nodes = new ArrayList<>(backwards.size());
        for (BlockPos c : backwards) {
            nodes.add(new Node(c.getX(), c.getY(), c.getZ()));
        }
        return nodes;
    }
}
