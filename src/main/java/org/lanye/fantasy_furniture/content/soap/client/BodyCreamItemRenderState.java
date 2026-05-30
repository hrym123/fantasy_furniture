package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;

/** 物品栏 Geo 渲染：当前堆栈外观（线程局部）。 */
public final class BodyCreamItemRenderState {

    private static final ThreadLocal<BodyCreamAppearance> APPEARANCE = new ThreadLocal<>();

    private BodyCreamItemRenderState() {}

    public static void set(BodyCreamAppearance appearance) {
        APPEARANCE.set(appearance);
    }

    public static BodyCreamAppearance appearance() {
        BodyCreamAppearance value = APPEARANCE.get();
        return value != null ? value : BodyCreamAppearance.defaults();
    }

    public static void clear() {
        APPEARANCE.remove();
    }
}
