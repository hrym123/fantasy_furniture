package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.soap.block.DisplayCabinetBlock;

/** 沐浴露 / 洗发露对打开态陈列柜右击：绕过 {@link net.minecraft.world.item.BlockItem} 的相邻放置。 */
public final class DisplayCabinetBottleInsert {

    private DisplayCabinetBottleInsert() {}

    /**
     * 若目标为已打开陈列柜，将交互交给方块 {@link DisplayCabinetBlock#use}。
     *
     * @return 非 null 时物品侧应直接返回该结果；否则继续默认 {@code useOn}
     */
    @Nullable
    public static InteractionResult useOnOpenCabinet(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof DisplayCabinetBlock) || !state.getValue(DisplayCabinetBlock.OPEN)) {
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
