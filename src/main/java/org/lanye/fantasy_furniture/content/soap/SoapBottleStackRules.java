package org.lanye.fantasy_furniture.content.soap;

import java.util.List;

/**
 * 瓶罐摞层数与特殊场合 2/3 载体规则。
 *
 * <p>规则：乳霜 / 沐浴露 / 洗发露可任意组合、任意顺序摆放；默认上限 {@link SoapBottleKind#MIXED_MAX_STACK}（4）。
 * 特例 1：已有 4 瓶乳霜（纯乳霜摞）时，可再放第 5 瓶乳霜（上限 {@link BodyCreamAssets#MAX_STACK}）。
 * 特例 2：第 3 位为乳霜且无载体时，第 4 位可放架或盒。
 * 特例 3：恰好 2 瓶且无载体时可先放架/盒（中间态），再仅可放乳霜进入完成态（同特例 2）。
 */
public final class SoapBottleStackRules {

    private SoapBottleStackRules() {}

    /**
     * 在现有层上再叠 {@code incomingKind} 时的允许上限。
     *
     * <p>已有 4 瓶乳霜且再叠乳霜 → 5；其余任意组合 → 4。
     */
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

    /** 当前摞的层数上限（不看下一瓶种类）。纯乳霜 → 5，否则 → 4。 */
    public static int maxStackFor(List<SoapBottleLayer> layers) {
        if (layers.isEmpty()) {
            return BodyCreamAssets.MAX_STACK;
        }
        if (isHomogeneousCream(layers)) {
            return BodyCreamAssets.MAX_STACK;
        }
        return SoapBottleKind.MIXED_MAX_STACK;
    }

    /** 特殊 2：三瓶且顶层乳霜、尚无载体 → 可放架/盒进入完成态。 */
    public static boolean isSpecial2CarrierReady(SoapBottleStackData data) {
        return data.carrier() == null
                && data.layerCount() == 3
                && topIsCream(data.layersView());
    }

    /** 特殊 3 中间：恰好两瓶、尚无载体 → 可放架/盒进入中间态。 */
    public static boolean isSpecial3IntermediateReady(SoapBottleStackData data) {
        return data.carrier() == null && data.layerCount() == 2;
    }

    /** 完成态：有载体且非中间态（三瓶 + 第 4 位架/盒）。 */
    public static boolean isCarrierCompleted(SoapBottleStackData data) {
        return data.carrier() != null && !data.carrierIntermediate();
    }

    public static boolean canAcceptBottle(SoapBottleStackData data, SoapBottleKind kind) {
        if (data.carrier() != null) {
            // 中间态仅可再接受乳霜（走完成路径）；完成态不再接受瓶。
            return data.carrierIntermediate() && kind == SoapBottleKind.BODY_CREAM;
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

    /** 纯乳霜摞且层数 ≥2 时才走 {@code 乳霜_堆叠_x5} 管线（含第 5 陈列位）。 */
    public static boolean usesCreamFiveSlotStack(List<SoapBottleLayer> layers) {
        return isHomogeneousCream(layers) && layers.size() >= 2;
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

    /**
     * 混合摞或单层非宿主种类：须按层合并各陈列位体素，不可整摞用宿主 {@link SoapStackCollisionShapes} 层数查表。
     */
    public static boolean needsPerLayerStackCollision(List<SoapBottleLayer> layers, SoapBottleKind hostKind) {
        if (layers.isEmpty()) {
            return false;
        }
        if (isMixed(layers)) {
            return true;
        }
        return layers.get(0).kind() != hostKind;
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

    private static boolean topIsCream(List<SoapBottleLayer> layers) {
        if (layers.isEmpty()) {
            return false;
        }
        return layers.get(layers.size() - 1).kind() == SoapBottleKind.BODY_CREAM;
    }

    /** 现有层全是乳霜，且再叠的也是乳霜 → 允许到第 5 瓶。 */
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
