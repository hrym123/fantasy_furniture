package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

/**
 * 幻想调试棒：按准心拆除/恢复柜子隔板。
 */
public final class CabinetShelfDebugActions {

    private CabinetShelfDebugActions() {}

    /**
     * @param reverse true（潜行）时尝试恢复准心处隔板；否则拆除。
     * @return 成功时的动作条消息；未命中隔板等返回 empty（由调用方决定 FAIL/PASS）。
     */
    public static Optional<Component> tryRemoveShelf(
            Level level, BlockPos hitPos, Player player, BlockHitResult hit, boolean reverse) {
        BlockState state = level.getBlockState(hitPos);
        if (!(state.getBlock() instanceof CabinetBlock cabinet)) {
            return Optional.empty();
        }
        BlockPos master = CabinetBlock.masterPos(state, hitPos);
        BlockEntity raw = level.getBlockEntity(master);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return Optional.empty();
        }
        CabinetKind kind = cabinet.kind();
        Direction facing = state.getValue(CabinetBlock.FACING);
        int shelf = kind.pickShelf(
                master,
                facing,
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f),
                be.shelvesMask(),
                reverse);
        if (shelf < 0) {
            return Optional.empty();
        }
        if (reverse) {
            if (!be.restoreShelf(shelf)) {
                return Optional.empty();
            }
            level.playSound(null, master, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7f, 1.1f);
            return Optional.of(
                    Component.translatable("debug.fantasy_furniture.cabinet.shelf_restored", shelf));
        }
        if (!be.removeShelf(shelf)) {
            return Optional.empty();
        }
        level.playSound(null, master, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.7f, 1.2f);
        return Optional.of(
                Component.translatable("debug.fantasy_furniture.cabinet.shelf_removed", shelf));
    }
}
