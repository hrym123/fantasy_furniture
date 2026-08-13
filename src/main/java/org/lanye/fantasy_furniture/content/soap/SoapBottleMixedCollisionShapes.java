package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 瓶罐摞：按<strong>槽号</strong>合并北向体素（空槽跳过）。 */
public final class SoapBottleMixedCollisionShapes {

    private SoapBottleMixedCollisionShapes() {}

    public static VoxelShape north(SoapBottleStackData data) {
        return northExcludingSlot(data, -1);
    }

    /** @deprecated 稠密列表；请用 {@link #north(SoapBottleStackData)} */
    @Deprecated
    public static VoxelShape north(List<SoapBottleLayer> layers) {
        return northExcludingSlot(layers, -1);
    }

    public static VoxelShape northExcludingSlot(SoapBottleStackData data, int excludeSlotOneBased) {
        VoxelShape shape = Shapes.empty();
        int limit = SoapBottleStackData.MAX_SLOTS;
        for (int i = 0; i < limit; i++) {
            SoapBottleLayer layer = data.slotAt(i);
            if (layer == null) {
                continue;
            }
            int slot = i + 1;
            if (slot == excludeSlotOneBased) {
                continue;
            }
            shape = Shapes.or(shape, slotNorth(layer.kind(), slot));
        }
        return shape;
    }

    @Deprecated
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
