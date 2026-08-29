package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 电脑北向碰撞：{@code geo_collision_box.py --gecko-block} 外接盒（关 / 开各一）。
 */
public final class ComputerCollisionShapes {

    private static final VoxelShape COMPUTER_1_CLOSED = Block.box(0.0, 0.0, 0.0, 16.0, 12.5, 16.0);
    private static final VoxelShape COMPUTER_1_OPEN = Block.box(0.0, 0.0, 0.0, 16.0, 12.6, 16.0);
    private static final VoxelShape COMPUTER_2_CLOSED = Block.box(0.0, 0.0, 2.0, 16.0, 13.3, 16.0);
    private static final VoxelShape COMPUTER_2_OPEN = Block.box(0.0, 0.0, 2.0, 16.0, 13.3, 16.0);

    private ComputerCollisionShapes() {}

    public static VoxelShape north(String closedAssetId, boolean open) {
        return switch (closedAssetId) {
            case "computer_1" -> open ? COMPUTER_1_OPEN : COMPUTER_1_CLOSED;
            case "computer_2" -> open ? COMPUTER_2_OPEN : COMPUTER_2_CLOSED;
            default -> COMPUTER_1_CLOSED;
        };
    }
}
