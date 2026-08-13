package org.lanye.fantasy_furniture.content.soap.client;

import javax.annotation.Nullable;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;

/** 瓶罐摞 carrier overlay 渲染态（ThreadLocal）。 */
public final class SoapBottleCarrierRenderState {

    private static final ThreadLocal<SoapStackCarrierKind> KIND = new ThreadLocal<>();
    private static final ThreadLocal<Integer> BOX_MAT = ThreadLocal.withInitial(() -> 1);
    private static final ThreadLocal<Boolean> INTERMEDIATE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> BOX_OPEN = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<SoapBarAppearance> SOAP = new ThreadLocal<>();

    private SoapBottleCarrierRenderState() {}

    public static void set(
            SoapStackCarrierKind kind,
            int boxMaterialId,
            boolean intermediate,
            boolean boxOpen,
            @Nullable SoapBarAppearance soap) {
        KIND.set(kind);
        BOX_MAT.set(boxMaterialId);
        INTERMEDIATE.set(intermediate);
        BOX_OPEN.set(boxOpen);
        SOAP.set(soap);
    }

    public static void clear() {
        KIND.remove();
        BOX_MAT.remove();
        INTERMEDIATE.remove();
        BOX_OPEN.remove();
        SOAP.remove();
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

    public static boolean boxOpen() {
        return Boolean.TRUE.equals(BOX_OPEN.get());
    }

    @Nullable
    public static SoapBarAppearance soap() {
        return SOAP.get();
    }

    public static boolean hasSoap() {
        return SOAP.get() != null;
    }
}
