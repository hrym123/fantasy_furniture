package org.lanye.fantasy_furniture.content.sweeper.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

/**
 * 扫地机寻路几何与行为态枚举的单元测试：不加载 {@link SweeperRobotEntity}，避免实体子系统静态初始化。
 */
class SweeperRobotPathAndBehaviorTest {

    @Test
    void sweeperRobotState_byOrdinal_roundTripsDefinedStates() {
        for (SweeperRobotState s : SweeperRobotState.values()) {
            assertSame(s, SweeperRobotState.byOrdinal(s.ordinal()));
        }
    }

    @Test
    void sweeperRobotState_byOrdinal_invalidFallsBackToIdle() {
        assertSame(SweeperRobotState.IDLE, SweeperRobotState.byOrdinal(-1));
        assertSame(SweeperRobotState.IDLE, SweeperRobotState.byOrdinal(SweeperRobotState.values().length));
        assertSame(SweeperRobotState.IDLE, SweeperRobotState.byOrdinal(999));
    }

    @Test
    void sweeperRobotState_migrateLegacyOrdinal_matchesPreSplitLayout() {
        assertSame(SweeperRobotState.DOCKED, SweeperRobotState.migrateLegacyOrdinal(0, false));
        assertSame(SweeperRobotState.RETURNING_LOW_HEALTH, SweeperRobotState.migrateLegacyOrdinal(1, false));
        assertSame(SweeperRobotState.RETURNING_CACHE_FULL, SweeperRobotState.migrateLegacyOrdinal(1, true));
        assertSame(SweeperRobotState.COLLECTING, SweeperRobotState.migrateLegacyOrdinal(2, false));
        assertSame(SweeperRobotState.PATROLLING, SweeperRobotState.migrateLegacyOrdinal(3, false));
        assertSame(SweeperRobotState.IDLE, SweeperRobotState.migrateLegacyOrdinal(4, false));
        assertSame(SweeperRobotState.EXITING_DOCK, SweeperRobotState.migrateLegacyOrdinal(5, false));
        assertSame(SweeperRobotState.REENTERING_PATROL, SweeperRobotState.migrateLegacyOrdinal(6, false));
    }

    @Test
    void sweeperRobotState_declarationOrder_alignsWithBehaviorOneThroughSeven() {
        SweeperRobotState[] v = SweeperRobotState.values();
        assertSame(SweeperRobotState.DOCKED, v[0]);
        assertSame(SweeperRobotState.RETURNING_LOW_HEALTH, v[1]);
        assertSame(SweeperRobotState.EXITING_DOCK, v[2]);
        assertSame(SweeperRobotState.RETURNING_CACHE_FULL, v[3]);
        assertSame(SweeperRobotState.COLLECTING, v[4]);
        assertSame(SweeperRobotState.REENTERING_PATROL, v[5]);
        assertSame(SweeperRobotState.IDLE, v[6]);
        assertSame(SweeperRobotState.PATROLLING, v[7]);
    }

    @Test
    void sweeperItemGroundPath_exposesExactTerminalPosition() {
        BlockPos target = new BlockPos(4, 64, -2);
        Vec3 exact = new Vec3(4.25, 64.1, -1.75);
        List<Node> nodes = List.of(new Node(1, 64, 0), new Node(4, 64, -2));
        SweeperItemGroundPath path = new SweeperItemGroundPath(nodes, target, true, exact);
        assertEquals(2, path.getNodeCount());
        assertEquals(target, path.getTarget());
        assertEquals(exact.x, path.exactItemPosition().x, 1e-9);
        assertEquals(exact.y, path.exactItemPosition().y, 1e-9);
        assertEquals(exact.z, path.exactItemPosition().z, 1e-9);
    }

    @Test
    void collectWaypointCenter_matchesBlockCenter() {
        Node n = new Node(10, 65, -3);
        Vec3 c = SweeperRobotPathHelpers.collectWaypointCenter(n);
        assertEquals(10.5, c.x, 1e-9);
        assertEquals(65.5, c.y, 1e-9);
        assertEquals(-2.5, c.z, 1e-9);
    }

    @Test
    void xzSnapGoalToBlockCenter_preservesY_snapsXZ() {
        Vec3 goal = new Vec3(1.9, 72.3, -0.1);
        Vec3 snapped = SweeperRobotPathHelpers.xzSnapGoalToBlockCenter(goal);
        assertEquals(72.3, snapped.y, 1e-9);
        assertEquals(1.5, snapped.x, 1e-9);
        assertEquals(-0.5, snapped.z, 1e-9);
    }

    @Test
    void collectGroundPathWaypoint_lastNodeOnItemPath_usesExactPosition() {
        BlockPos target = new BlockPos(2, 64, 2);
        Vec3 exact = new Vec3(2.1, 64.0, 2.2);
        List<Node> nodes = List.of(new Node(0, 64, 0), new Node(2, 64, 2));
        SweeperItemGroundPath sip = new SweeperItemGroundPath(nodes, target, true, exact);
        Vec3 last = SweeperRobotPathHelpers.collectGroundPathWaypoint(sip, 1);
        assertEquals(exact.x, last.x, 1e-9);
        assertEquals(exact.y, last.y, 1e-9);
        assertEquals(exact.z, last.z, 1e-9);
        Vec3 first = SweeperRobotPathHelpers.collectGroundPathWaypoint(sip, 0);
        assertEquals(0.5, first.x, 1e-9);
        assertEquals(64.5, first.y, 1e-9);
        assertEquals(0.5, first.z, 1e-9);
    }

    @Test
    void collectGroundPathWaypoint_plainPath_usesBlockCentersOnly() {
        BlockPos target = new BlockPos(1, 64, 0);
        List<Node> nodes = List.of(new Node(0, 64, 0), new Node(1, 64, 0));
        Path plain = new Path(nodes, target, true);
        Vec3 end = SweeperRobotPathHelpers.collectGroundPathWaypoint(plain, 1);
        assertEquals(1.5, end.x, 1e-9);
        assertEquals(64.5, end.y, 1e-9);
        assertEquals(0.5, end.z, 1e-9);
    }

    @Test
    void cardinalWallSlideUnit_prefersTangentAlignedWithPreference() {
        Vec3 v = SweeperRobotPathHelpers.cardinalWallSlideUnit(1.0, 0.0, 0.0, 1.0);
        assertEquals(0.0, v.x, 1e-9);
        assertEquals(1.0, v.z, 1e-9);
        assertEquals(0.0, v.y, 1e-9);
    }

    @Test
    void cardinalWallSlideUnit_zeroPreference_yieldsUnitCardinalXZ() {
        Vec3 v = SweeperRobotPathHelpers.cardinalWallSlideUnit(0.0, 1.0, 0.0, 0.0);
        assertTrue(v.lengthSqr() > 0.99);
        assertEquals(0.0, v.y, 1e-9);
        assertEquals(0.0, v.z, 1e-9);
        assertTrue(Math.abs(v.x) > 0.5);
    }

    @Test
    void wallClimbTiltQuaternion_nonHorizontal_returnsIdentity() {
        Quaternionf q = SweeperRobotPathHelpers.wallClimbTiltQuaternion(Direction.UP, 45f);
        assertEquals(1f, q.w, 1e-5f);
        assertEquals(0f, q.x, 1e-5f);
        assertEquals(0f, q.y, 1e-5f);
        assertEquals(0f, q.z, 1e-5f);
    }

    @Test
    void wallClimbTiltQuaternion_nullWall_returnsIdentity() {
        Quaternionf q = SweeperRobotPathHelpers.wallClimbTiltQuaternion(null, 0f);
        assertEquals(1f, q.w, 1e-5f);
    }

    @Test
    void wallClimbTiltQuaternion_horizontalWall_producesUnitQuaternion() {
        Quaternionf q = SweeperRobotPathHelpers.wallClimbTiltQuaternion(Direction.EAST, 33f);
        assertNotNull(q);
        float lenSq = q.lengthSquared();
        assertTrue(lenSq > 0.99f && lenSq < 1.01f, "应为单位四元数");
    }
}
