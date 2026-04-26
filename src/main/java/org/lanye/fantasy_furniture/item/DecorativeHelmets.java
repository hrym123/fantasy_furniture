package org.lanye.fantasy_furniture.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 装饰用头盔（头饰）共通判定：与 {@link DecorativeHelmetItem}、玩家 Layer、{@code CustomHeadLayer} Mixin 共用同一规则。
 */
public final class DecorativeHelmets {

    private DecorativeHelmets() {}

    public static boolean isDecorativeHelmet(Item item) {
        return item instanceof DecorativeHelmetItem;
    }

    public static boolean isDecorativeHelmet(ItemStack stack) {
        return isDecorativeHelmet(stack.getItem());
    }
}
