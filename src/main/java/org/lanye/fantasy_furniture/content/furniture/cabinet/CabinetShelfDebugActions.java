package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

/**
 * 幻想调试棒：按准心拆除/恢复柜子隔板（柜子1 可拆本段底板/连接中隔/顶盖）。
 * 横向连接态下同节同行整行同步拆除/恢复。
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
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return Optional.empty();
        }
        CabinetJointShelfPick.Hit shelfHit =
                CabinetJointShelfPick.pickAnyShelf(
                        level,
                        hitPos,
                        state,
                        player.getEyePosition(1.0f),
                        player.getViewVector(1.0f),
                        hit.getLocation(),
                        reverse);
        if (shelfHit == null) {
            return Optional.empty();
        }
        BlockPos owner = shelfHit.ownerPos();
        BlockEntity raw = level.getBlockEntity(owner);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return Optional.empty();
        }
        int shelf = shelfHit.shelf();
        Direction facing = shelfHit.facing();
        List<BlockPos> row = horizontalRow(level, owner, facing);

        if (reverse) {
            boolean any = false;
            for (BlockPos p : row) {
                if (level.getBlockEntity(p) instanceof CabinetBlockEntity cell && cell.restoreShelf(shelf)) {
                    any = true;
                }
            }
            if (!any) {
                return Optional.empty();
            }
            level.playSound(null, owner, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7f, 1.1f);
            return Optional.of(
                    Component.translatable("debug.fantasy_furniture.cabinet.shelf_restored", shelf));
        }

        boolean any = false;
        for (BlockPos p : row) {
            if (level.getBlockEntity(p) instanceof CabinetBlockEntity cell && cell.removeShelf(shelf)) {
                any = true;
            }
        }
        if (!any) {
            return Optional.empty();
        }
        level.playSound(null, owner, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.7f, 1.2f);
        return Optional.of(
                Component.translatable("debug.fantasy_furniture.cabinet.shelf_removed", shelf));
    }

    /**
     * 本格 + 同节水平邻接链上的全部柜子1（横向连接行，受 {@link CabinetKind#CABINET_1_MAX_STACK} 分组）。
     */
    static List<BlockPos> horizontalRow(LevelReader level, BlockPos origin, Direction facing) {
        List<BlockPos> out = new ArrayList<>();
        out.add(origin);
        walkLinked(level, origin, facing, facing.getClockWise(), out::add);
        walkLinked(level, origin, facing, facing.getCounterClockWise(), out::add);
        return out;
    }

    private static void walkLinked(
            LevelReader level,
            BlockPos origin,
            Direction facing,
            Direction step,
            Consumer<BlockPos> visitor) {
        BlockPos p = origin.relative(step);
        while (CabinetBlock.areLinkedInRow(level, origin, p, facing)) {
            visitor.accept(p);
            p = p.relative(step);
        }
    }
}
