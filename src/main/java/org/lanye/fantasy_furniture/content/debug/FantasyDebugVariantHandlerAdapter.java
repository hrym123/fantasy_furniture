package org.lanye.fantasy_furniture.content.debug;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.lanye.reverie_core.debug.FantasyDebugVariantHandler;

final class FantasyDebugVariantHandlerAdapter implements FantasyDebugVariantHandler {

    private final BlockCycler blockCycler;
    private final ItemCycler itemCycler;

    FantasyDebugVariantHandlerAdapter(BlockCycler blockCycler, ItemCycler itemCycler) {
        this.blockCycler = blockCycler;
        this.itemCycler = itemCycler;
    }

    @Override
    public Optional<Component> cycleBlock(Level level, BlockPos pos, boolean reverse) {
        return blockCycler.cycle(level, pos, reverse);
    }

    @Override
    public Optional<Component> cycleItemStack(ItemStack stack, boolean reverse) {
        return itemCycler.cycle(stack, reverse);
    }

    @FunctionalInterface
    interface BlockCycler {
        Optional<Component> cycle(Level level, BlockPos pos, boolean reverse);
    }

    @FunctionalInterface
    interface ItemCycler {
        Optional<Component> cycle(ItemStack stack, boolean reverse);
    }
}
