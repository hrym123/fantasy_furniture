package org.lanye.fantasy_furniture.content.soap;

import java.util.List;

/**
 * 瓶罐摞槽位与特殊场合 2/3 载体规则（定长可空槽）。
 *
 * <p>规则：乳霜 / 沐浴露 / 洗发露可任意组合；常规最多占 4 槽；特例 1：纯乳霜可至第 5 槽。
 * 特例 2：槽 2（第 3 陈列位）为乳霜且无载体时可挂架/盒。
 * 特例 3：恰好 2 瓶且无载体时可先挂架/盒（中间态），再仅可放乳霜入槽 2。
 */
public final class SoapBottleStackRules {

    private SoapBottleStackRules() {}

    /** 推入 {@code incoming} 时可用的槽上界（不含）：4 或 5。 */
    public static int maxSlotIndexExclusiveForPush(SoapBottleStackData data, SoapBottleKind incoming) {
        if (wouldBeHomogeneousCream(data, incoming)) {
            return BodyCreamAssets.MAX_STACK;
        }
        return SoapBottleKind.MIXED_MAX_STACK;
    }

    public static int maxSlotIndexExclusive(SoapBottleStackData data) {
        if (data.layerCount() == 0) {
            return BodyCreamAssets.MAX_STACK;
        }
        if (isHomogeneousCream(data)) {
            return BodyCreamAssets.MAX_STACK;
        }
        return SoapBottleKind.MIXED_MAX_STACK;
    }

    public static int maxStackFor(List<SoapBottleLayer> layers, SoapBottleKind incomingKind) {
        if (layers.isEmpty()) {
            return incomingKind == SoapBottleKind.BODY_CREAM
                    ? BodyCreamAssets.MAX_STACK
                    : SoapBottleKind.MIXED_MAX_STACK;
        }
        if (wouldBeHomogeneousCream(layers, incomingKind)) {
            return BodyCreamAssets.MAX_STACK;
        }
        return SoapBottleKind.MIXED_MAX_STACK;
    }

    public static int maxStackFor(List<SoapBottleLayer> layers) {
        if (layers.isEmpty()) {
            return BodyCreamAssets.MAX_STACK;
        }
        if (isHomogeneousCream(layers)) {
            return BodyCreamAssets.MAX_STACK;
        }
        return SoapBottleKind.MIXED_MAX_STACK;
    }

    /** 特殊 2：占满 3 瓶、槽 2 为乳霜、尚无载体。 */
    public static boolean isSpecial2CarrierReady(SoapBottleStackData data) {
        if (data.carrier() != null || data.layerCount() != 3) {
            return false;
        }
        SoapBottleLayer slot2 = data.slotAt(2);
        return slot2 != null && slot2.kind() == SoapBottleKind.BODY_CREAM;
    }

    public static boolean isSpecial3IntermediateReady(SoapBottleStackData data) {
        return data.carrier() == null && data.layerCount() == 2;
    }

    public static boolean isCarrierCompleted(SoapBottleStackData data) {
        return data.carrier() != null && !data.carrierIntermediate();
    }

    public static boolean canAcceptBottle(SoapBottleStackData data, SoapBottleKind kind) {
        if (data.carrier() != null) {
            return data.carrierIntermediate()
                    && kind == SoapBottleKind.BODY_CREAM
                    && data.slotAt(2) == null;
        }
        return data.layerCount() < maxStackFor(data.layersView(), kind);
    }

    public static boolean canAcceptCarrier(
            SoapBottleStackData data, SoapStackCarrierKind carrier) {
        if (carrier == null || data.carrier() != null) {
            return false;
        }
        return isSpecial2CarrierReady(data) || isSpecial3IntermediateReady(data);
    }

    public static boolean usesCreamFiveSlotStack(SoapBottleStackData data) {
        return isHomogeneousCream(data) && data.layerCount() >= 2;
    }

    public static boolean usesCreamFiveSlotStack(List<SoapBottleLayer> layers) {
        return isHomogeneousCream(layers) && layers.size() >= 2;
    }

    public static boolean isMixed(SoapBottleStackData data) {
        return isMixed(data.layersView());
    }

    public static boolean isMixed(List<SoapBottleLayer> layers) {
        if (layers.size() <= 1) {
            return false;
        }
        SoapBottleKind first = layers.get(0).kind();
        for (int i = 1; i < layers.size(); i++) {
            if (layers.get(i).kind() != first) {
                return true;
            }
        }
        return false;
    }

    public static boolean needsPerLayerStackCollision(SoapBottleStackData data, SoapBottleKind hostKind) {
        if (data.layerCount() == 0) {
            return false;
        }
        if (data.hasSparseHoles() || data.hasCarrier()) {
            return true;
        }
        // 单瓶不在槽 0：陈列位有偏移，不能走宿主 LAYERS 查表单瓶形
        if (data.firstOccupiedSlot() > 0) {
            return true;
        }
        return needsPerLayerStackCollision(data.layersView(), hostKind);
    }

    public static boolean needsPerLayerStackCollision(List<SoapBottleLayer> layers, SoapBottleKind hostKind) {
        if (layers.isEmpty()) {
            return false;
        }
        if (isMixed(layers)) {
            return true;
        }
        return layers.get(0).kind() != hostKind;
    }

    public static boolean isHomogeneousCream(SoapBottleStackData data) {
        return isHomogeneousCream(data.layersView());
    }

    public static boolean isHomogeneousCream(List<SoapBottleLayer> layers) {
        if (layers.isEmpty()) {
            return false;
        }
        for (SoapBottleLayer layer : layers) {
            if (layer.kind() != SoapBottleKind.BODY_CREAM) {
                return false;
            }
        }
        return true;
    }

    private static boolean wouldBeHomogeneousCream(SoapBottleStackData data, SoapBottleKind incomingKind) {
        if (incomingKind != SoapBottleKind.BODY_CREAM) {
            return false;
        }
        if (data.layerCount() == 0) {
            return true;
        }
        return isHomogeneousCream(data);
    }

    private static boolean wouldBeHomogeneousCream(List<SoapBottleLayer> layers, SoapBottleKind incomingKind) {
        if (incomingKind != SoapBottleKind.BODY_CREAM) {
            return false;
        }
        for (SoapBottleLayer layer : layers) {
            if (layer.kind() != SoapBottleKind.BODY_CREAM) {
                return false;
            }
        }
        return true;
    }
}
