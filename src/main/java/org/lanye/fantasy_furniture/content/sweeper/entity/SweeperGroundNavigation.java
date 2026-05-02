package org.lanye.fantasy_furniture.content.sweeper.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.sweeper.ai.InternalPathTuning;

/**
 * 扫地机器人专用地面导航器：收集路径使用「以实体碰撞箱为判据、**仅对方块几何**」的格点 **A\***（含水平对角邻接），
 * 不调用原版 {@link GroundPathNavigation#createPath}；失败时退化为两点直线。航点仅使用 {@link Vec3#atCenterOf(BlockPos)}，
 * 不向掉落物亚格点坐标驱动。
 */
public final class SweeperGroundNavigation extends GroundPathNavigation {
    /** 单次网格搜索最多 **弹出** 次数（A*）。 */
    private static final int COLLECT_GRID_MAX_EXPANSIONS = 8192;

    private record GridSearchEntry(BlockPos pos, int g, int f) {}

    /** 寻路用 AABB 相对碰撞箱在 XZ 上的微膨胀，减轻墙角数值边界导致的误判挡。 */
    private static final double NAV_AABB_INFLATE_XZ = 0.02D;

    public SweeperGroundNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    /**
     * 在三维格点上做 A*，每一步用 {@link net.minecraft.world.level.CollisionGetter#getBlockCollisions} 检测
     * 「脚底落在该格的机器人碰撞箱（含 {@link InternalPathTuning.Sweeper} 半径补偿）」是否与 **方块** 固体相交（
     * 不含其它实体，避免收集物自身挡格）；成功则构造
     * {@link SweeperItemGroundPath}，末档航点为 **路径终点方块中心**（{@link Vec3#atCenterOf}）。若目标格本身不可站，
     * 则在目标 14 邻格中选距起点最近的可站格作为搜索终点，便于凹角内掉落物仍可达。无路则退化为起点—目标格直线两节点。
     */
    public Path createPathToExactPos(Vec3 targetPos) {
        if (this.mob.getY() < (double) this.level.getMinBuildHeight()) {
            return null;
        }

        int gx = Mth.floor(targetPos.x);
        int gy = Mth.floor(targetPos.y);
        int gz = Mth.floor(targetPos.z);
        BlockPos goalBlock = new BlockPos(gx, gy, gz);
        BlockPos start = this.mob.blockPosition();
        BlockPos footGoal = resolveCollectFootGoal(start, goalBlock);
        Vec3 terminalCenter =
                footGoal == null ? Vec3.atCenterOf(goalBlock) : Vec3.atCenterOf(footGoal);

        if (footGoal != null && start.equals(footGoal)) {
            List<Node> one = new ArrayList<>(1);
            one.add(new Node(footGoal.getX(), footGoal.getY(), footGoal.getZ()));
            return new SweeperItemGroundPath(one, goalBlock, true, terminalCenter);
        }

        List<Node> wide = footGoal == null ? null : findWideBodyGridPath(start, footGoal);
        if (wide != null && !wide.isEmpty()) {
            Node last = wide.get(wide.size() - 1);
            terminalCenter = Vec3.atCenterOf(new BlockPos(last.x, last.y, last.z));
            return new SweeperItemGroundPath(wide, goalBlock, true, terminalCenter);
        }

        Vec3 goalCenter = Vec3.atCenterOf(goalBlock);
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(start.getX(), gy, start.getZ()));
        nodes.add(new Node(gx, gy, gz));
        return new SweeperItemGroundPath(nodes, goalBlock, true, goalCenter);
    }

    /**
     * 收集路径的「脚底目标格」：优先物品所在格；若宽体 {@link #isFootCellNavigable} 不可站，则在 {@link
     * #collectGridNeighbors}（14 邻）中选距 {@code start} Chebyshev 最近的可站格。
     */
    @Nullable
    private BlockPos resolveCollectFootGoal(BlockPos start, BlockPos itemBlock) {
        if (isFootCellNavigable(itemBlock)) {
            return itemBlock;
        }
        BlockPos best = null;
        int bestCheb = Integer.MAX_VALUE;
        for (BlockPos n : collectGridNeighbors(itemBlock)) {
            if (!isFootCellNavigable(n)) {
                continue;
            }
            int h = chebHeuristic(start, n);
            if (h < bestCheb) {
                bestCheb = h;
                best = n;
            }
        }
        return best;
    }

    /**
     * 以 {@link #isFootCellNavigable} 为可走条件，从 {@code start} 到 {@code goal} 做 **A\***（每边代价 1，启发式为 Chebyshev
     * 距离，可纳）。
     */
    @Nullable
    private List<Node> findWideBodyGridPath(BlockPos start, BlockPos goal) {
        Map<BlockPos, BlockPos> parent = new HashMap<>(256);
        Map<BlockPos, Integer> gScore = new HashMap<>(256);
        Comparator<GridSearchEntry> byBestFirst =
                Comparator.comparingInt(GridSearchEntry::f).thenComparingInt(GridSearchEntry::g);
        PriorityQueue<GridSearchEntry> open = new PriorityQueue<>(byBestFirst);

        gScore.put(start, 0);
        parent.put(start, start);
        open.add(new GridSearchEntry(start, 0, chebHeuristic(start, goal)));

        int expansions = 0;
        while (!open.isEmpty()) {
            GridSearchEntry cur = open.poll();
            int curG = cur.g;
            Integer bestG = gScore.get(cur.pos);
            if (bestG == null || curG != bestG) {
                continue;
            }
            if (++expansions > COLLECT_GRID_MAX_EXPANSIONS) {
                return null;
            }
            if (cur.pos.equals(goal)) {
                return buildNodeChain(parent, start, goal);
            }
            for (BlockPos nxt : collectGridNeighbors(cur.pos)) {
                if (!isFootCellNavigable(nxt)) {
                    continue;
                }
                int tentativeG = curG + 1;
                if (tentativeG >= gScore.getOrDefault(nxt, Integer.MAX_VALUE)) {
                    continue;
                }
                gScore.put(nxt, tentativeG);
                parent.put(nxt, cur.pos);
                open.add(new GridSearchEntry(nxt, tentativeG, tentativeG + chebHeuristic(nxt, goal)));
            }
        }
        return null;
    }

    /** Chebyshev 距离（与 A* 启发式一致）；包级可见供单元测试。 */
    static int chebHeuristic(BlockPos a, BlockPos b) {
        return Math.max(
                Math.abs(a.getX() - b.getX()),
                Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ())));
    }

    /**
     * 水平四向 + 竖直 + 同层四对角：对角步在凸角外侧常为正交集所无法连通的捷径，仍由 {@link #isFootCellNavigable} 用实体 AABB
     * 把关，避免穿墙。包级可见供单元测试。
     */
    static List<BlockPos> collectGridNeighbors(BlockPos p) {
        List<BlockPos> out = new ArrayList<>(14);
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
        return out;
    }

    /** 由 parent 指针链重建 A* 路径节点序列；包级可见供单元测试。 */
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

    /** 与 {@link SweeperRobotEntity} 中 {@code robotCollisionRadius} 一致，用于寻路碰撞箱水平半宽。 */
    private static double pathCollisionHalfWidth(Mob mob) {
        double configured = InternalPathTuning.Sweeper.COLLISION_RADIUS;
        double auto = mob.getBbWidth() * 0.5D;
        double base = configured > 0.0D ? configured : auto;
        return base + InternalPathTuning.Sweeper.PATH_RADIUS_BONUS;
    }

    /**
     * 脚底落在 {@code feet} 方块底面中心时，用加宽后的水平 AABB 与 **方块碰撞体** 相交检测；不统计其它实体（否则
     * 收集目标上的 {@link net.minecraft.world.entity.item.ItemEntity} 会把目标格判为不可走，机器人顶墙或表现为与
     * 「自身/掉落物」卡死）。仍传入 {@code mob} 以正确解析门、雪层等依赖实体的方块碰撞形状。
     */
    private boolean isFootCellNavigable(BlockPos feet) {
        if (feet.getY() < this.level.getMinBuildHeight() || feet.getY() > this.level.getMaxBuildHeight()) {
            return false;
        }
        Vec3 bottomCenter = Vec3.atBottomCenterOf(feet);
        double half = pathCollisionHalfWidth(this.mob);
        double h = this.mob.getBbHeight();
        AABB aabb =
                new AABB(
                                bottomCenter.x - half,
                                bottomCenter.y,
                                bottomCenter.z - half,
                                bottomCenter.x + half,
                                bottomCenter.y + h,
                                bottomCenter.z + half)
                        .inflate(NAV_AABB_INFLATE_XZ, 1.0e-4D, NAV_AABB_INFLATE_XZ);
        for (VoxelShape shape : this.level.getBlockCollisions(this.mob, aabb)) {
            if (!shape.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** 兼容原有调用：将方块格转换为中心点后走统一精确坐标入口。 */
    @Override
    public Path createPath(BlockPos pPos, int pAccuracy) {
        return createPathToExactPos(Vec3.atCenterOf(pPos));
    }
}
