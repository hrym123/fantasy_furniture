package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;

/** 包装盒物品栏 Geo 渲染：按 NBT 盒色注入贴图。 */
public final class SoapPaperBoxItemRenderState {

    private static final ThreadLocal<SoapPaperBoxAppearance> APPEARANCE = new ThreadLocal<>();

    private SoapPaperBoxItemRenderState() {}

    public static void set(SoapPaperBoxAppearance appearance) {
        APPEARANCE.set(appearance);
    }

    public static SoapPaperBoxAppearance appearance() {
        SoapPaperBoxAppearance value = APPEARANCE.get();
        return value != null ? value : SoapPaperBoxAppearance.defaults();
    }

    public static void clear() {
        APPEARANCE.remove();
    }
}
