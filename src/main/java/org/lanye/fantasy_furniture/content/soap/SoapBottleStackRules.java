package org.lanye.fantasy_furniture.content.soap;

import java.util.List;

/**
 * 瓶罐摞层数上限。
 *
 * <p>规则：乳霜 / 沐浴露 / 洗发露可任意组合、任意顺序摆放；默认上限 {@link SoapBottleKind#MIXED_MAX_STACK}（4）。
 * 特例：已有 4 瓶乳霜（纯乳霜摞）时，可再放第 5 瓶乳霜（上限 {@link BodyCreamAssets#MAX_STACK}）。
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
            return incomingKind == SoapBottleKind.BODY_CREAM ? BodyCreamAssets.MAX_STACK : SoapBottleKind.MIXED_MAX_STACK;
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
