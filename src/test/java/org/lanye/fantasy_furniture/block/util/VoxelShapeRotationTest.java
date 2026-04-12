package org.lanye.fantasy_furniture.block.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

class VoxelShapeRotationTest {

    @Test
    void rotate_fourQuarterTurns_restoresAabbs() {
        VoxelShape shape = Block.box(2, 0, 4, 14, 8, 12);
        VoxelShape current = shape;
        for (int i = 0; i < 4; i++) {
            current = VoxelShapeRotation.rotate(current, Rotation.CLOCKWISE_90);
        }
        assertAabbListsEqual(shape.toAabbs(), current.toAabbs());
    }

    @Test
    void rotateYFromNorth_north_isIdentity() {
        VoxelShape shape = Block.box(1, 2, 3, 10, 11, 12);
        VoxelShape out = VoxelShapeRotation.rotateYFromNorth(shape, net.minecraft.core.Direction.NORTH);
        assertAabbListsEqual(shape.toAabbs(), out.toAabbs());
    }

    private static void assertAabbListsEqual(List<AABB> a, List<AABB> b) {
        assertEquals(a.size(), b.size());
        List<AABB> sa = new ArrayList<>(a);
        List<AABB> sb = new ArrayList<>(b);
        Comparator<AABB> cmp =
                Comparator.comparingDouble(AABB::minX)
                        .thenComparingDouble(AABB::minY)
                        .thenComparingDouble(AABB::minZ);
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
