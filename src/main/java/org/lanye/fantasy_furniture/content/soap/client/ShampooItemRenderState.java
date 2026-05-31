package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;

/** 物品栏 Geo 渲染：当前堆栈外观（线程局部）。 */
public final class ShampooItemRenderState {

    private static final ThreadLocal<ShampooAppearance> APPEARANCE = new ThreadLocal<>();

    private ShampooItemRenderState() {}

    public static void set(ShampooAppearance appearance) {
        APPEARANCE.set(appearance);
    }

    public static ShampooAppearance appearance() {
        ShampooAppearance value = APPEARANCE.get();
        return value != null ? value : ShampooAppearance.defaults();
    }

    public static void clear() {
        APPEARANCE.remove();
    }
}
