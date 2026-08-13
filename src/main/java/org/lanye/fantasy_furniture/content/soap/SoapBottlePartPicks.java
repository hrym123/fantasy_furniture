package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.composite.CompositePartId;
import org.lanye.reverie_core.composite.PartPickEntry;

/**
 * 瓶罐摞准心选取：按<strong>槽号</strong>生成条目（空槽不生成）。
 */
public final class SoapBottlePartPicks {

    private SoapBottlePartPicks() {}

    public static List<PartPickEntry> entries(SoapBottleStackData data) {
        List<PartPickEntry> list = new ArrayList<>();
        boolean completedCream =
                SoapBottleStackRules.isCarrierCompleted(data)
                        && data.slotAt(2) != null
                        && data.slotAt(2).kind() == SoapBottleKind.BODY_CREAM;

        for (int i = 0; i < SoapBottleStackData.MAX_SLOTS; i++) {
            SoapBottleLayer layer = data.slotAt(i);
            if (layer == null) {
                continue;
            }
            VoxelShape shape;
            if (completedCream && i == 2) {
                shape = SoapBottleCarrierCollisionShapes.COMBO_CREAM;
            } else {
                shape = slotNorth(layer.kind(), i + 1);
            }
            // 瓶优先于载体（重叠时准心/泵动画落在瓶上；入皂靠持皂优先，不依赖载体抢 tier）
            int tier = 10 + (SoapBottleStackData.MAX_SLOTS - 1 - i);
            list.add(PartPickEntry.of(SoapBottleParts.bottle(i), shape, tier));
        }

        SoapStackCarrierKind carrier = data.carrier();
        if (carrier != null) {
            VoxelShape carrierShape =
                    SoapBottleCarrierCollisionShapes.carrierNorth(carrier, data.carrierIntermediate());
            list.add(PartPickEntry.of(SoapBottleParts.CARRIER, carrierShape, 100));
        }
        return list;
    }

    public static List<CompositePartId> activeParts(SoapBottleStackData data) {
        List<CompositePartId> ids = new ArrayList<>();
        for (int i = 0; i < SoapBottleStackData.MAX_SLOTS; i++) {
            if (data.slotAt(i) != null) {
                ids.add(SoapBottleParts.bottle(i));
            }
        }
        if (data.hasCarrier()) {
            ids.add(SoapBottleParts.CARRIER);
        }
        return ids;
    }

    private static VoxelShape slotNorth(SoapBottleKind kind, int slotOneBased) {
        return switch (kind) {
            case BODY_WASH -> SoapStackCollisionShapes.bodyWashSlotNorth(slotOneBased);
            case SHAMPOO -> SoapStackCollisionShapes.shampooSlotNorth(slotOneBased);
            case BODY_CREAM -> SoapStackCollisionShapes.bodyCreamSlotNorth(slotOneBased);
        };
    }
}
