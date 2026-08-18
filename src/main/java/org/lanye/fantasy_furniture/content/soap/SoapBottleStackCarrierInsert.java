package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

/**
 * 架 / 盒对瓶罐摞右击：绕过 {@link net.minecraft.world.item.BlockItem} 的相邻放置，转交方块 {@code use}。
 *
 * <p>含「点下方支撑顶面」路径，见 {@link SoapBottleStackClickThrough}。
 */
public final class SoapBottleStackCarrierInsert {

    private SoapBottleStackCarrierInsert() {}

    /**
     * 若目标为瓶罐摞（或其下方支撑顶面），将交互交给该方块（由 {@link SoapBottleStackUse} 处理架/盒）。
     *
     * @return 非 null 时物品侧应直接返回该结果；否则继续默认 {@code useOn}
     */
    @Nullable
    public static InteractionResult useOnBottleStack(UseOnContext context) {
        return SoapBottleStackClickThrough.useOnStackOrSupportBelow(context);
    }
}