package org.lanye.fantasy_furniture.common.seat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/**
 * 验证 {@link SeatConfig} 的几何换算：相对方块的入座 AABB、坐骑世界坐标与朝向函数在锚点下的行为是否正确。
 * <p>
 * 纯数学与断言，不访问 {@link net.minecraft.world.level.Level}。
 */
class SeatConfigTest {

    @Test
    void toWorldSitRange_offsetsAnchor() {
        SeatConfig cfg =
                new SeatConfig(
                        s -> true,
                        new AABB(0.1, 0.2, 0.3, 0.7, 0.8, 0.9),
                        Vec3.ZERO,
                        st -> 0f);
        BlockPos pos = new BlockPos(5, 10, -3);
        AABB w = cfg.toWorldSitRange(pos);
        assertEquals(5.1, w.minX, 1e-9);
        assertEquals(10.2, w.minY, 1e-9);
        assertEquals(-2.7, w.minZ, 1e-9);
        assertEquals(5.7, w.maxX, 1e-9);
        assertEquals(10.8, w.maxY, 1e-9);
        assertEquals(-2.1, w.maxZ, 1e-9);
    }

    @Test
    void seatWorldPosition_addsBlockMin() {
        SeatConfig cfg =
                new SeatConfig(
                        s -> true,
                        new AABB(0, 0, 0, 1, 1, 1),
                        new Vec3(0.5, 0.35, 0.6),
                        st -> 0f);
        Vec3 p = cfg.seatWorldPosition(new BlockPos(100, 64, 200));
        assertEquals(100.5, p.x, 1e-9);
        assertEquals(64.35, p.y, 1e-9);
        assertEquals(200.6, p.z, 1e-9);
    }

    /** 与 {@link SeatInteraction} 中入座判定一致：玩家 AABB 与 {@link SeatConfig#toWorldSitRange} 相交。 */
    @Test
    void playerIntersectsSitRange_whenStandingInBox() {
        SeatConfig cfg =
                new SeatConfig(
                        s -> true,
                        new AABB(0, 0, 0, 1, 1, 1),
                        new Vec3(0.5, 0, 0.5),
                        st -> 0f);
        BlockPos anchor = new BlockPos(0, 64, 0);
        AABB sit = cfg.toWorldSitRange(anchor);
        AABB playerFeet = new AABB(0.2, 64.0, 0.2, 0.8, 65.8, 0.8);
        assertTrue(playerFeet.intersects(sit));
    }

    @Test
    void playerDoesNotIntersectSitRange_whenOutsideBox() {
        SeatConfig cfg =
                new SeatConfig(
                        s -> true,
                        new AABB(0, 0, 0, 0.3, 1, 1),
                        Vec3.ZERO,
                        st -> 0f);
        BlockPos anchor = BlockPos.ZERO;
        AABB sit = cfg.toWorldSitRange(anchor);
        AABB player = new AABB(0.5, 0, 0, 1.0, 2, 1);
        assertFalse(player.intersects(sit));
    }

    @Test
    void blockValid_respectsPredicate() {
        SeatConfig allowed =
                new SeatConfig(s -> true, new AABB(0, 0, 0, 1, 1, 1), Vec3.ZERO, st -> 0f);
        SeatConfig denied =
                new SeatConfig(s -> false, new AABB(0, 0, 0, 1, 1, 1), Vec3.ZERO, st -> 0f);
        assertTrue(allowed.blockValid().test(null));
        assertFalse(denied.blockValid().test(null));
    }
}
