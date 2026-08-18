package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 瓶罐摞叠放 / 插载体：准心点在摞上，或点在摞<strong>下方支撑方块顶面</strong>时，转交摞方块 {@code use}。
 *
 * <p>避免必须精确点中瓶罐模型才能叠加（U015 追加）。
 */
public final class SoapBottleStackClickThrough {

    private SoapBottleStackClickThrough() {}

    /**
     * @return 非 null 时物品侧应直接返回；否则继续默认 {@code useOn}
     */
    @Nullable
    public static InteractionResult useOnStackOrSupportBelow(UseOnContext context) {
        InteractionResult onClicked = useAtStack(context, context.getClickedPos());
        if (onClicked != null) {
            return onClicked;
        }
        if (context.getClickedFace() != Direction.UP) {
            return null;
        }
        return useAtStack(context, context.getClickedPos().above());
    }

    @Nullable
    private static InteractionResult useAtStack(UseOnContext context, BlockPos stackPos) {
        BlockEntity be = context.getLevel().getBlockEntity(stackPos);
        if (!(be instanceof SoapBottleStackUse.Holder)) {
            return null;
        }
        BlockState state = context.getLevel().getBlockState(stackPos);
        BlockHitResult hit =
                new BlockHitResult(
                        context.getClickLocation(), context.getClickedFace(), stackPos, false);
        return state.use(context.getLevel(), context.getPlayer(), context.getHand(), hit);
    }
}
