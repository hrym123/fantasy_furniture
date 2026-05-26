package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;

/** 手持包装袋物品渲染时的当前外观（线程局部）。 */
public final class SoapPaperBagItemRenderState {

    private static final ThreadLocal<SoapPaperBagAppearance> CURRENT = new ThreadLocal<>();

    private SoapPaperBagItemRenderState() {}

    public static void set(SoapPaperBagAppearance appearance) {
        CURRENT.set(appearance);
    }

    public static SoapPaperBagAppearance get() {
        SoapPaperBagAppearance a = CURRENT.get();
        return a != null ? a : SoapPaperBagAppearance.defaults();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
