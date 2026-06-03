package org.lanye.fantasy_furniture.content.soap;

import java.util.List;

/** 瓶罐摞层数上限与是否允许混合摞放。 */
public final class SoapBottleStackRules {

    private SoapBottleStackRules() {}

    public static int maxStackFor(List<SoapBottleLayer> layers, SoapBottleKind incomingKind) {
        if (layers.isEmpty()) {
            return incomingKind == SoapBottleKind.BODY_CREAM ? BodyCreamAssets.MAX_STACK : SoapBottleKind.MIXED_MAX_STACK;
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
