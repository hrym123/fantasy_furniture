package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;

/** 物品栏 Geo 渲染：当前堆栈外观（线程局部）。 */
public final class BodyWashItemRenderState {

    private static final ThreadLocal<BodyWashAppearance> APPEARANCE = new ThreadLocal<>();

    private BodyWashItemRenderState() {}

    public static void set(BodyWashAppearance appearance) {
        APPEARANCE.set(appearance);
    }

    public static BodyWashAppearance appearance() {
        BodyWashAppearance value = APPEARANCE.get();
        return value != null ? value : BodyWashAppearance.defaults();
    }

    public static void clear() {
        APPEARANCE.remove();
    }
}
