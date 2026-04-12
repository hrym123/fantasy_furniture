package org.lanye.fantasy_furniture.block.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

/**
 * 验证 {@link VoxelShapeRotation} 的几何正确性：
 * <ul>
 *   <li>绕 Y 轴连续旋转四次 90° 后，碰撞盒集合应与原形状一致（闭合性）；</li>
 *   <li>从「北向基准」旋转时，若朝向已为北，结果应与恒等变换一致。</li>
 * </ul>
 * 使用 {@link Shapes#box} 构造体素，不依赖 {@link net.minecraft.server.Bootstrap}。
 */
class VoxelShapeRotationTest {

    @Test
    void rotate_fourQuarterTurns_restoresAabbs() {
        VoxelShape shape = Shapes.box(2 / 16.0, 0, 4 / 16.0, 14 / 16.0, 8 / 16.0, 12 / 16.0);
        VoxelShape current = shape;
        for (int i = 0; i < 4; i++) {
            current = VoxelShapeRotation.rotate(current, Rotation.CLOCKWISE_90);
        }
        assertAabbListsEqual(shape.toAabbs(), current.toAabbs());
    }

    @Test
    void rotateYFromNorth_north_isIdentity() {
        VoxelShape shape = Shapes.box(1 / 16.0, 2 / 16.0, 3 / 16.0, 10 / 16.0, 11 / 16.0, 12 / 16.0);
        VoxelShape out = VoxelShapeRotation.rotateYFromNorth(shape, net.minecraft.core.Direction.NORTH);
        assertAabbListsEqual(shape.toAabbs(), out.toAabbs());
    }

    private static void assertAabbListsEqual(List<AABB> a, List<AABB> b) {
        assertEquals(a.size(), b.size());
        List<AABB> sa = new ArrayList<>(a);
        List<AABB> sb = new ArrayList<>(b);
        Comparator<AABB> cmp =
                Comparator.<AABB>comparingDouble(box -> box.minX)
                        .thenComparingDouble(box -> box.minY)
                        .thenComparingDouble(box -> box.minZ);
        sa.sort(cmp);
        sb.sort(cmp);
        for (int i = 0; i < sa.size(); i++) {
            AABB x = sa.get(i);
            AABB y = sb.get(i);
            assertEquals(x.minX, y.minX, 1e-9);
            assertEquals(x.minY, y.minY, 1e-9);
            assertEquals(x.minZ, y.minZ, 1e-9);
            assertEquals(x.maxX, y.maxX, 1e-9);
            assertEquals(x.maxY, y.maxY, 1e-9);
            assertEquals(x.maxZ, y.maxZ, 1e-9);
        }
    }
}
