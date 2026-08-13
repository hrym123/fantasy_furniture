package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 混合瓶罐摞：按自底向上层序与每层种类合并对应陈列位北向体素。 */
public final class SoapBottleMixedCollisionShapes {

    private SoapBottleMixedCollisionShapes() {}

    public static VoxelShape north(List<SoapBottleLayer> layers) {
        return northExcludingSlot(layers, -1);
    }

    /**
     * 按层合并北向体素；{@code excludeSlotOneBased} 为正时跳过该陈列位（完成态第 3 位改用组合乳霜盒）。
     */
    public static VoxelShape northExcludingSlot(List<SoapBottleLayer> layers, int excludeSlotOneBased) {
        if (layers.isEmpty()) {
            return Shapes.empty();
        }
        VoxelShape shape = Shapes.empty();
        int limit = Math.min(layers.size(), SoapBottleKind.MIXED_MAX_STACK);
        for (int i = 0; i < limit; i++) {
            int slot = i + 1;
            if (slot == excludeSlotOneBased) {
                continue;
            }
            shape = Shapes.or(shape, slotNorth(layers.get(i).kind(), slot));
        }
        return shape;
    }

    private static VoxelShape slotNorth(SoapBottleKind kind, int slotOneBased) {
        return switch (kind) {
            case BODY_WASH -> SoapStackCollisionShapes.bodyWashSlotNorth(slotOneBased);
            case SHAMPOO -> SoapStackCollisionShapes.shampooSlotNorth(slotOneBased);
            case BODY_CREAM -> SoapStackCollisionShapes.bodyCreamSlotNorth(slotOneBased);
        };
    }
}
