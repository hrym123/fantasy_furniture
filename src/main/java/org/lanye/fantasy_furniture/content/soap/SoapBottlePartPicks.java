package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.composite.CompositePartId;
import org.lanye.reverie_core.composite.PartPickEntry;

/**
 * 瓶罐摞准心选取条目：碰撞体素即选取体素（绝对北向，已含组合位）。
 *
 * <p>瓶层 tier = 层序（上层优先）；载体 tier = 0（与瓶重叠时优先点到载体）。
 */
public final class SoapBottlePartPicks {

    private SoapBottlePartPicks() {}

    public static List<PartPickEntry> entries(SoapBottleStackData data) {
        List<PartPickEntry> list = new ArrayList<>();
        List<SoapBottleLayer> layers = data.layersView();
        boolean completedCream =
                SoapBottleStackRules.isCarrierCompleted(data)
                        && layers.size() >= 3
                        && layers.get(2).kind() == SoapBottleKind.BODY_CREAM;

        int n = Math.min(layers.size(), SoapBottleKind.MIXED_MAX_STACK);
        if (SoapBottleStackRules.isHomogeneousCream(layers)) {
            n = Math.min(layers.size(), BodyCreamAssets.MAX_STACK);
        }
        for (int i = 0; i < n; i++) {
            VoxelShape shape;
            if (completedCream && i == 2) {
                shape = SoapBottleCarrierCollisionShapes.COMBO_CREAM;
            } else {
                shape = slotNorth(layers.get(i).kind(), i + 1);
            }
            // 上层瓶优先于下层（tier 更小）
            int tier = 100 + (n - 1 - i);
            list.add(PartPickEntry.of(SoapBottleParts.bottle(i), shape, tier));
        }

        SoapStackCarrierKind carrier = data.carrier();
        if (carrier != null) {
            VoxelShape carrierShape =
                    SoapBottleCarrierCollisionShapes.carrierNorth(carrier, data.carrierIntermediate());
            list.add(PartPickEntry.of(SoapBottleParts.CARRIER, carrierShape, 0));
        }
        return list;
    }

    public static List<CompositePartId> activeParts(SoapBottleStackData data) {
        List<CompositePartId> ids = new ArrayList<>();
        for (int i = 0; i < data.layerCount(); i++) {
            ids.add(SoapBottleParts.bottle(i));
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
