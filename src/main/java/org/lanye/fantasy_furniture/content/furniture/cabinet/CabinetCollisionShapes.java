package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 柜子北向碰撞：每格一份（柜子1型竖向三格；柜子2型单格薄片）。
 *
 * <p>柜子1型 geo 仍自底格绘出 0..48；上层格仅提供可点选碰撞，无 BE。
 */
public final class CabinetCollisionShapes {

    private static final VoxelShape CABINET_1_CELL = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    /** 与柜子2 geo 一致：中心相对 z∈[1,8]/16 → 方块像素 z 9..16。 */
    private static final VoxelShape CABINET_2 = Block.box(0.0, 0.0, 9.0, 16.0, 16.0, 16.0);

    private CabinetCollisionShapes() {}

    public static VoxelShape north(CabinetKind kind) {
        return switch (kind) {
            case CABINET_1 -> CABINET_1_CELL;
            case CABINET_2 -> CABINET_2;
        };
    }
}
