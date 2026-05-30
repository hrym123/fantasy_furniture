package org.lanye.fantasy_furniture.content.soap.item;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.lanye.fantasy_furniture.content.soap.debug.SoapDebugStickActions;

/** 肥皂套系调试棒：右键已放置包装盒切换堆叠样式（默认样式 1）。 */
public final class SoapDebugStickItem extends Item {

    public SoapDebugStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Optional<Component> message =
                SoapDebugStickActions.cyclePaperBoxStackStyle(
                        level, context.getClickedPos(), player.isShiftKeyDown());
        if (message.isEmpty()) {
            return InteractionResult.PASS;
        }
        player.displayClientMessage(message.get(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
