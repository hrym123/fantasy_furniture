package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 柜子北向碰撞：{@code geo_collision_box.py --gecko-block} 外接盒。
 *
 * <p>柜子1型 geo 已将 bbmodel Y=-16..32 上移 16，落在 0..48（三格高）。
 */
public final class CabinetCollisionShapes {

    private static final VoxelShape CABINET_1 = Block.box(0.0, 0.0, 0.0, 16.0, 48.0, 16.0);
    private static final VoxelShape CABINET_2 = Block.box(0.0, 0.0, 9.0, 16.0, 16.0, 16.0);

    private CabinetCollisionShapes() {}

    public static VoxelShape north(CabinetKind kind) {
        return switch (kind) {
            case CABINET_1 -> CABINET_1;
            case CABINET_2 -> CABINET_2;
        };
    }
}
