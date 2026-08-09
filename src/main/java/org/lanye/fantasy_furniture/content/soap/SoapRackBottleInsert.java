package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;

/** 瓶罐对肥皂架右击：绕过 {@link net.minecraft.world.item.BlockItem} 的相邻放置。 */
public final class SoapRackBottleInsert {

    private SoapRackBottleInsert() {}

    /**
     * 若目标为肥皂架，将交互交给方块 {@link SoapRackBlock#use}。
     *
     * @return 非 null 时物品侧应直接返回该结果；否则继续默认 {@code useOn}
     */
    @Nullable
    public static InteractionResult useOnRack(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof SoapRackBlock)) {
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
