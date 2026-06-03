package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 混合瓶罐摞：按自底向上层序与每层种类合并对应陈列位北向体素。 */
public final class SoapBottleMixedCollisionShapes {

    private SoapBottleMixedCollisionShapes() {}

    public static VoxelShape north(List<SoapBottleLayer> layers) {
        if (layers.isEmpty()) {
            return Shapes.empty();
        }
        VoxelShape shape = Shapes.empty();
        int limit = Math.min(layers.size(), SoapBottleKind.MIXED_MAX_STACK);
        for (int i = 0; i < limit; i++) {
            shape = Shapes.or(shape, slotNorth(layers.get(i).kind(), i + 1));
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
