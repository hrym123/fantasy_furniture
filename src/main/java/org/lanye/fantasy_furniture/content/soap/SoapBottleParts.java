package org.lanye.fantasy_furniture.content.soap;

import org.lanye.reverie_core.composite.CompositePartId;

/** 瓶罐摞组合分件 id（供 core 选取 / 布局）。 */
public final class SoapBottleParts {

    public static final String LAYOUT_FREE = "free_stack";
    public static final String LAYOUT_CARRIER_MID = "carrier_mid";
    public static final String LAYOUT_CARRIER_DONE = "carrier_done";

    public static final CompositePartId CARRIER = CompositePartId.of("carrier");

    private SoapBottleParts() {}

    public static CompositePartId bottle(int indexFromBottom) {
        return CompositePartId.of("bottle_" + indexFromBottom);
    }

    public static boolean isCarrier(CompositePartId id) {
        return CARRIER.equals(id);
    }

    /** @return 瓶层下标；非 bottle_* 返回 -1 */
    public static int bottleIndex(CompositePartId id) {
        String raw = id.id();
        if (!raw.startsWith("bottle_")) {
            return -1;
        }
        try {
            return Integer.parseInt(raw.substring("bottle_".length()));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String layoutKey(SoapBottleStackData data) {
        if (data.carrier() == null) {
            return LAYOUT_FREE;
        }
        return data.carrierIntermediate() ? LAYOUT_CARRIER_MID : LAYOUT_CARRIER_DONE;
    }
}
