package org.lanye.fantasy_furniture.content.debug.item;

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
import org.lanye.fantasy_furniture.content.debug.ModVariantCycler;

/**
 * 开发用：右键方块循环本模组模型变体；右键空气循环副手物品模型变体（主手持棒）。不切换材质/颜料。
 */
public final class DebugVariantStickItem extends Item {

    public DebugVariantStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
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
                ModVariantCycler.cycleBlock(
                        level, context.getClickedPos(), player.isShiftKeyDown());
        if (message.isEmpty()) {
            return InteractionResult.PASS;
        }
        player.displayClientMessage(message.get(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);
        Optional<Component> message = ModVariantCycler.cycleItemStack(target, player.isShiftKeyDown());
        if (message.isEmpty()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        player.displayClientMessage(message.get(), true);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
