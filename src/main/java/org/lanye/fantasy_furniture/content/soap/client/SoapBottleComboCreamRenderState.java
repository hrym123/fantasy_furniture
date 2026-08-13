package org.lanye.fantasy_furniture.content.soap.client;

/** 组合完成态第 3 位乳霜 overlay 材质（ThreadLocal）。 */
public final class SoapBottleComboCreamRenderState {

    private static final ThreadLocal<Integer> MATERIAL = ThreadLocal.withInitial(() -> 1);

    private SoapBottleComboCreamRenderState() {}

    public static void set(int materialId) {
        MATERIAL.set(materialId);
    }

    public static void clear() {
        MATERIAL.remove();
    }

    public static int materialId() {
        return MATERIAL.get();
    }
}
