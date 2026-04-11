package org.lanye.fantasy_furniture.block;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 水平 {@link Direction} 下将「北向基准」{@link VoxelShape} 绕 Y 轴旋转，与
 * {@link net.minecraft.world.level.block.HorizontalDirectionalBlock} 的 {@code FACING} 一致。
 */
public final class VoxelShapeRotation {

    private VoxelShapeRotation() {}

    public static VoxelShape rotateYFromNorth(VoxelShape northShape, Direction facing) {
        return rotate(northShape, rotationFromHorizontalNorth(facing));
    }

    /** 绕 Y 轴按 {@link Rotation} 旋转（俯视与方块状态旋转一致）。 */
    public static VoxelShape rotate(VoxelShape shape, Rotation rotation) {
        if (rotation == Rotation.NONE) {
            return shape;
        }
        List<AABB> boxes = shape.toAabbs();
        VoxelShape out = Shapes.empty();
        for (AABB box : boxes) {
            AABB rotated =
                    switch (rotation) {
                        case CLOCKWISE_90 -> rotateAabbY90(box);
                        case CLOCKWISE_180 -> rotateAabbY180(box);
                        case COUNTERCLOCKWISE_90 -> rotateAabbY270(box);
                        default -> box;
                    };
            out = Shapes.or(out, aabbToShape(rotated));
        }
        return out;
    }

    private static Rotation rotationFromHorizontalNorth(Direction facing) {
        return switch (facing) {
            case NORTH -> Rotation.NONE;
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static VoxelShape aabbToShape(AABB box) {
        return Block.box(
                box.minX * 16.0,
                box.minY * 16.0,
                box.minZ * 16.0,
                box.maxX * 16.0,
                box.maxY * 16.0,
                box.maxZ * 16.0);
    }

    private static AABB rotateAabbY90(AABB box) {
        double minX = box.minX;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxZ = box.maxZ;
        return new AABB(1.0 - maxZ, box.minY, minX, 1.0 - minZ, box.maxY, maxX);
    }

    private static AABB rotateAabbY180(AABB box) {
        return new AABB(1.0 - box.maxX, box.minY, 1.0 - box.maxZ, 1.0 - box.minX, box.maxY, 1.0 - box.minZ);
    }

    private static AABB rotateAabbY270(AABB box) {
        double minX = box.minX;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxZ = box.maxZ;
        return new AABB(minZ, box.minY, 1.0 - maxX, maxZ, box.maxY, 1.0 - minX);
    }
}
