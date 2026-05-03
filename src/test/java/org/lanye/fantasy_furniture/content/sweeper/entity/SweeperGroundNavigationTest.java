package org.lanye.fantasy_furniture.content.sweeper.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import org.junit.jupiter.api.Test;

/**
 * {@link SweeperGroundNavigation} 的轻量单元测试：仅覆盖包级可见的纯几何/图辅助逻辑。
 * <p>
 * 收集求路已委托原版 {@link net.minecraft.world.entity.ai.navigation.GroundPathNavigation#createPath}；本类仅测保留的静态辅助。
 */
class SweeperGroundNavigationTest {

    @Test
    void chebHeuristic_sameCell_isZero() {
        BlockPos p = new BlockPos(7, 64, -3);
        assertEquals(0, SweeperGroundNavigation.chebHeuristic(p, p));
    }

    @Test
    void chebHeuristic_matchesChebyshevMaxAxis() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, -5, 2);
        assertEquals(5, SweeperGroundNavigation.chebHeuristic(a, b));
    }

    @Test
    void pickChordFootNearestToStart_prefersCloserChebyshev_tiePrefersStrict() {
        BlockPos start = new BlockPos(0, 64, 0);
        BlockPos strictFar = new BlockPos(10, 64, 0);
        BlockPos relaxedMid = new BlockPos(2, 64, 0);
        BlockPos coarseFar = new BlockPos(8, 64, 0);
        assertEquals(
                relaxedMid,
                SweeperGroundNavigation.pickChordFootNearestToStart(
                        start, strictFar, relaxedMid, coarseFar));
        BlockPos strictTie = new BlockPos(2, 64, 0);
        BlockPos relaxedTie = new BlockPos(0, 64, 2);
        assertEquals(
                strictTie,
                SweeperGroundNavigation.pickChordFootNearestToStart(start, strictTie, relaxedTie, null));
    }

    @Test
    void collectGridNeighbors_hasTwentyTwoUniqueOffsets() {
        BlockPos origin = new BlockPos(10, 64, 10);
        List<BlockPos> n = SweeperGroundNavigation.collectGridNeighbors(origin);
        assertEquals(22, n.size());
        Set<BlockPos> uniq = new HashSet<>(n);
        assertEquals(22, uniq.size(), "22 邻接应对应 22 个不同格点");
        assertTrue(n.contains(origin.north()));
        assertTrue(n.contains(origin.south()));
        assertTrue(n.contains(origin.east()));
        assertTrue(n.contains(origin.west()));
        assertTrue(n.contains(origin.above()));
        assertTrue(n.contains(origin.below()));
        int y = origin.getY();
        int x = origin.getX();
        int z = origin.getZ();
        assertTrue(n.contains(new BlockPos(x + 1, y, z + 1)));
        assertTrue(n.contains(new BlockPos(x + 1, y, z - 1)));
        assertTrue(n.contains(new BlockPos(x - 1, y, z + 1)));
        assertTrue(n.contains(new BlockPos(x - 1, y, z - 1)));
        assertTrue(n.contains(new BlockPos(x + 1, y + 1, z)));
        assertTrue(n.contains(new BlockPos(x - 1, y + 1, z)));
        assertTrue(n.contains(new BlockPos(x + 1, y - 1, z)));
        assertTrue(n.contains(new BlockPos(x - 1, y - 1, z)));
        assertTrue(n.contains(new BlockPos(x, y + 1, z + 1)));
        assertTrue(n.contains(new BlockPos(x, y + 1, z - 1)));
        assertTrue(n.contains(new BlockPos(x, y - 1, z + 1)));
        assertTrue(n.contains(new BlockPos(x, y - 1, z - 1)));
    }

    @Test
    void buildNodeChain_reconstructsForwardOrder() {
        BlockPos start = new BlockPos(0, 0, 0);
        BlockPos mid = new BlockPos(1, 0, 0);
        BlockPos goal = new BlockPos(2, 0, 0);
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        parent.put(start, start);
        parent.put(mid, start);
        parent.put(goal, mid);

        List<Node> chain = SweeperGroundNavigation.buildNodeChain(parent, start, goal);
        assertNotNull(chain);
        assertEquals(3, chain.size());
        assertEquals(0, chain.get(0).x);
        assertEquals(0, chain.get(0).y);
        assertEquals(0, chain.get(0).z);
        assertEquals(1, chain.get(1).x);
        assertEquals(2, chain.get(2).x);
    }

    @Test
    void buildNodeChain_returnsNull_whenParentBroken() {
        BlockPos start = new BlockPos(0, 0, 0);
        BlockPos goal = new BlockPos(1, 0, 0);
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        parent.put(start, start);
        parent.put(goal, goal);

        assertNull(SweeperGroundNavigation.buildNodeChain(parent, start, goal));
    }
}
