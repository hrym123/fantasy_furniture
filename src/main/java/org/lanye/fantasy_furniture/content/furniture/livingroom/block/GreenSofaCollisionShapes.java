package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.common.state.SofaPart;

/**
 * green_sofa.bbmodel 北向碰撞（排除 NotCollisionBox* 组；tools/collision/geo_collision_box.py）。
 * 三联沙发按水平分段裁切后映射到各 part 方块局部 [0,16]^3。
 */
final class GreenSofaCollisionShapes {

    private GreenSofaCollisionShapes() {}

    static final VoxelShape LEFT_NORTH = orParts(
            Block.box(2.00, 8.00, 1.00, 16.00, 8.20, 10.00),
            Block.box(15.17, 11.15, 11.00, 16.00, 14.61, 16.00),
            Block.box(0.00, 8.00, 11.00, 16.00, 14.00, 16.00),
            Block.box(13.36, 11.15, 11.00, 16.00, 14.61, 16.00),
            Block.box(2.00, 14.00, 11.00, 14.00, 16.00, 16.00),
            Block.box(0.00, 11.15, 11.00, 2.64, 14.61, 16.00),
            Block.box(3.00, 5.00, 0.00, 16.00, 8.00, 1.30),
            Block.box(0.00, 5.00, 0.00, 3.53, 8.00, 2.38),
            Block.box(0.00, 5.00, 1.30, 16.00, 8.00, 16.00),
            Block.box(0.00, 0.00, 15.00, 16.00, 5.00, 16.00),
            Block.box(0.00, 4.00, 0.00, 16.00, 5.00, 15.00),
            Block.box(0.00, 0.00, 0.00, 1.00, 4.00, 15.00),
            Block.box(15.00, 1.00, 0.00, 16.00, 4.00, 15.00),
            Block.box(1.00, 0.00, 0.00, 16.00, 1.00, 15.00),
            Block.box(1.00, 1.50, 0.00, 2.00, 4.00, 14.00),
            Block.box(1.00, 1.00, 0.00, 15.00, 1.50, 14.00),
            Block.box(2.00, 3.50, 0.00, 14.00, 4.00, 1.00),
            Block.box(10.00, 1.50, 0.00, 14.00, 3.50, 1.00),
            Block.box(2.00, 1.50, 0.00, 6.00, 3.50, 1.00),
            Block.box(1.00, 1.00, 14.00, 15.00, 4.00, 15.00),
            Block.box(6.00, 1.50, 0.00, 10.00, 2.50, 1.00),
            Block.box(14.00, 1.50, 0.00, 15.00, 4.00, 14.00)
    );

    static final VoxelShape CENTER_NORTH = orParts(
            Block.box(0.00, 8.00, 1.00, 16.00, 8.20, 10.00),
            Block.box(0.00, 8.00, 11.00, 16.00, 14.00, 16.00),
            Block.box(13.36, 11.15, 11.00, 16.00, 14.61, 16.00),
            Block.box(2.00, 14.00, 11.00, 14.00, 16.00, 16.00),
            Block.box(0.00, 11.15, 11.00, 2.64, 14.61, 16.00),
            Block.box(15.17, 11.15, 11.00, 16.00, 14.61, 16.00),
            Block.box(0.00, 11.15, 11.00, 0.83, 14.61, 16.00),
            Block.box(0.00, 5.00, 0.00, 16.00, 8.00, 1.30),
            Block.box(0.00, 5.00, 1.30, 16.00, 8.00, 16.00),
            Block.box(0.00, 0.00, 15.00, 16.00, 5.00, 16.00),
            Block.box(0.00, 4.00, 0.00, 16.00, 5.00, 15.00),
            Block.box(0.00, 1.00, 0.00, 1.00, 4.00, 15.00),
            Block.box(15.00, 1.00, 0.00, 16.00, 4.00, 15.00),
            Block.box(0.00, 0.00, 0.00, 16.00, 1.00, 15.00),
            Block.box(1.00, 1.00, 14.00, 15.00, 4.00, 15.00),
            Block.box(6.00, 1.50, 0.00, 10.00, 2.50, 1.00),
            Block.box(2.00, 3.50, 0.00, 14.00, 4.00, 1.00),
            Block.box(1.00, 1.50, 0.00, 2.00, 4.00, 14.00),
            Block.box(14.00, 1.50, 0.00, 15.00, 4.00, 14.00),
            Block.box(1.00, 1.00, 0.00, 15.00, 1.50, 14.00),
            Block.box(10.00, 1.50, 0.00, 14.00, 3.50, 1.00),
            Block.box(2.00, 1.50, 0.00, 6.00, 3.50, 1.00)
    );

    static final VoxelShape RIGHT_NORTH = orParts(
            Block.box(0.00, 8.00, 1.00, 14.00, 8.20, 10.00),
            Block.box(0.00, 11.15, 11.00, 0.83, 14.61, 16.00),
            Block.box(0.00, 8.00, 11.00, 16.00, 14.00, 16.00),
            Block.box(13.36, 11.15, 11.00, 16.00, 14.61, 16.00),
            Block.box(2.00, 14.00, 11.00, 14.00, 16.00, 16.00),
            Block.box(0.00, 11.15, 11.00, 2.64, 14.61, 16.00),
            Block.box(12.47, 5.00, 0.00, 16.00, 8.00, 2.38),
            Block.box(0.00, 5.00, 0.00, 13.00, 8.00, 1.30),
            Block.box(0.00, 5.00, 1.30, 16.00, 8.00, 16.00),
            Block.box(0.00, 0.00, 15.00, 16.00, 5.00, 16.00),
            Block.box(0.00, 4.00, 0.00, 15.00, 5.00, 15.00),
            Block.box(0.00, 1.00, 0.00, 1.00, 4.00, 15.00),
            Block.box(0.00, 0.00, 0.00, 15.00, 1.00, 15.00),
            Block.box(15.00, 0.00, 0.00, 16.00, 5.00, 15.00),
            Block.box(2.00, 1.50, 0.00, 6.00, 3.50, 1.00),
            Block.box(1.00, 1.00, 0.00, 15.00, 1.50, 14.00),
            Block.box(14.00, 1.50, 0.00, 15.00, 4.00, 14.00),
            Block.box(10.00, 1.50, 0.00, 14.00, 3.50, 1.00),
            Block.box(6.00, 1.50, 0.00, 10.00, 2.50, 1.00),
            Block.box(2.00, 3.50, 0.00, 14.00, 4.00, 1.00),
            Block.box(1.00, 1.00, 14.00, 15.00, 4.00, 15.00),
            Block.box(1.00, 1.50, 0.00, 2.00, 4.00, 14.00)
    );

    static VoxelShape northForPart(SofaPart part) {
        return switch (part) {
            case LEFT -> LEFT_NORTH;
            case CENTER -> CENTER_NORTH;
            case RIGHT -> RIGHT_NORTH;
        };
    }

    private static VoxelShape orParts(VoxelShape first, VoxelShape... rest) {
        VoxelShape s = first;
        for (VoxelShape p : rest) {
            s = Shapes.or(s, p);
        }
        return s;
    }
}
