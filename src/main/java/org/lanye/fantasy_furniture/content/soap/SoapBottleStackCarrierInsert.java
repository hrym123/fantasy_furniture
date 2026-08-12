package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 架 / 盒对瓶罐摞右击：绕过 {@link net.minecraft.world.item.BlockItem} 的相邻放置，转交方块 {@code use}。
 */
public final class SoapBottleStackCarrierInsert {

    private SoapBottleStackCarrierInsert() {}

    /**
     * 若目标为瓶罐摞 BE，将交互交给该方块（由 {@link SoapBottleStackUse} 处理架/盒）。
     *
     * @return 非 null 时物品侧应直接返回该结果；否则继续默认 {@code useOn}
     */
    @Nullable
    public static InteractionResult useOnBottleStack(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(be instanceof SoapBottleStackUse.Holder)) {
            return null;
        }
        BlockHitResult hit =
                new BlockHitResult(
                        context.getClickLocation(),
                        context.getClickedFace(),
                        context.getClickedPos(),
                        false);
        return state.use(context.getLevel(), context.getPlayer(), context.getHand(), hit);
    }
}
