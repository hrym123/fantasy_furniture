package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;

/** 瓶罐摞 carrier overlay 渲染态（ThreadLocal）。 */
public final class SoapBottleCarrierRenderState {

    private static final ThreadLocal<SoapStackCarrierKind> KIND = new ThreadLocal<>();
    private static final ThreadLocal<Integer> BOX_MAT = ThreadLocal.withInitial(() -> 1);
    private static final ThreadLocal<Boolean> INTERMEDIATE = ThreadLocal.withInitial(() -> false);

    private SoapBottleCarrierRenderState() {}

    public static void set(SoapStackCarrierKind kind, int boxMaterialId, boolean intermediate) {
        KIND.set(kind);
        BOX_MAT.set(boxMaterialId);
        INTERMEDIATE.set(intermediate);
    }

    public static void clear() {
        KIND.remove();
        BOX_MAT.remove();
        INTERMEDIATE.remove();
    }

    public static SoapStackCarrierKind kind() {
        return KIND.get();
    }

    public static int boxMaterialId() {
        return BOX_MAT.get();
    }

    public static boolean intermediate() {
        return INTERMEDIATE.get();
    }
}
