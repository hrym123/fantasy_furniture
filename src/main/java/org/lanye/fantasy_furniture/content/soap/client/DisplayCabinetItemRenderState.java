package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAppearance;

/** 陈列柜物品栏 Geo 渲染：按 NBT 盒色注入贴图。 */
public final class DisplayCabinetItemRenderState {

    private static final ThreadLocal<DisplayCabinetAppearance> APPEARANCE = new ThreadLocal<>();

    private DisplayCabinetItemRenderState() {}

    public static void set(DisplayCabinetAppearance appearance) {
        APPEARANCE.set(appearance);
    }

    public static DisplayCabinetAppearance appearance() {
        DisplayCabinetAppearance value = APPEARANCE.get();
        return value != null ? value : DisplayCabinetAppearance.defaults();
    }

    public static void clear() {
        APPEARANCE.remove();
    }
}
