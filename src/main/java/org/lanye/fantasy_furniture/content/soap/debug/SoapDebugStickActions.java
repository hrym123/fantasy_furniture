package org.lanye.fantasy_furniture.content.soap.debug;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAssets;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;

/** 肥皂调试棒：切换已放置包装盒摞的堆叠样式（1 / 2）。 */
public final class SoapDebugStickActions {

    private SoapDebugStickActions() {}

    public static Optional<Component> cyclePaperBoxStackStyle(Level level, BlockPos pos, boolean reverse) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SoapPaperBoxBlock block)) {
            return Optional.empty();
        }
        int current = state.getValue(block.STACK_STYLE);
        int next = SoapPaperBoxAssets.nextStackStyle(current, reverse);
        if (next == current) {
            return Optional.empty();
        }
        level.setBlock(pos, state.setValue(block.STACK_STYLE, next), Block.UPDATE_ALL);
        return Optional.of(
                Component.translatable("debug.fantasy_furniture.variant.soap_paper_box_stack_style", next));
    }
}
