package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 杯具北向碰撞：各堆叠档 geo {@code --gecko-block} 外接盒。
 *
 * <p>重生成：{@code python collision/geo_collision_box.py …/drinkware[_xN].geo.json --gecko-block}
 */
public final class DrinkwareCollisionShapes {

    public static final int MAX_STACK = 4;

    private static final VoxelShape STACK_1 = Block.box(5.88, 0.00, 5.38, 10.12, 3.50, 9.62);
    private static final VoxelShape STACK_2 = Block.box(1.88, 0.00, 6.38, 14.12, 3.50, 10.62);
    private static final VoxelShape STACK_3 = Block.box(1.88, 0.00, 3.08, 14.12, 3.50, 12.75);
    private static final VoxelShape STACK_4 = Block.box(1.25, 0.00, 1.25, 14.75, 3.50, 14.75);

    private DrinkwareCollisionShapes() {}

    public static VoxelShape north(int stack) {
        return switch (clamp(stack)) {
            case 2 -> STACK_2;
            case 3 -> STACK_3;
            case 4 -> STACK_4;
            default -> STACK_1;
        };
    }

    public static int clamp(int stack) {
        if (stack < 1) {
            return 1;
        }
        if (stack > MAX_STACK) {
            return MAX_STACK;
        }
        return stack;
    }

    /** 堆叠档资源 stem：1→{@code drinkware}，2～4→{@code drinkware_xN}。 */
    public static String assetId(int stack) {
        int s = clamp(stack);
        return s <= 1 ? "drinkware" : "drinkware_x" + s;
    }
}
