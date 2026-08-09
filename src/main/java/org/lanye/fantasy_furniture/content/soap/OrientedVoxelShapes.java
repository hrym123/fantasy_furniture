package org.lanye.fantasy_furniture.content.soap;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 缓存「北向常量体素 × 水平朝向」旋转结果，避免 {@code getShape} 热路径反复 {@code Shapes.or}。
 * 仅用于表驱动北向形状（同引用复用）；混合摞仍走 BE 侧缓存。
 */
public final class OrientedVoxelShapes {

    private static final ConcurrentHashMap<Long, VoxelShape> CACHE = new ConcurrentHashMap<>();

    private OrientedVoxelShapes() {}

    public static VoxelShape geckoFromNorth(VoxelShape north, Direction facing) {
        if (facing == Direction.NORTH) {
            return north;
        }
        long key = (((long) System.identityHashCode(north)) << 3) | facing.get2DDataValue();
        return CACHE.computeIfAbsent(
                key, k -> VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing));
    }
}
