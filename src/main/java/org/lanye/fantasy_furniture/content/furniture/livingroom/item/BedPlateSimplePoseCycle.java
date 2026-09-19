package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateBedFootPos;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlatePillowMode;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowHost;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.PickedLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.Plate;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;

/**
 * 床板 2/3/4：调试棒对准枕头，整套在平放、竖放、斜放之间切换。
 * 当前件数放不下的字母会跳过，并提示当前模式。对准木架则不睡。
 */
public final class BedPlateSimplePoseCycle {

    private BedPlateSimplePoseCycle() {}

    public static InteractionResult cycle(
            Plate plate,
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(BedPlateBedFootPos.footPos(state, pos));
        if (be instanceof BedPlate2BlockEntity plate2) {
            return cyclePlate2(plate, level, state, pos, player, hit, plate2);
        }
        if (be instanceof BedPlateSheetPillowHost host) {
            return cycleHost(plate, level, state, pos, player, hit, host);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult cycleHost(
            Plate plate,
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            BedPlateSheetPillowHost host) {
        BedPlateSheetPillowSlots slots = host.sheetPillows();
        PickedLayer layer = pick(plate, state, host.hasDuvet(), hostHasCover(host), slots, hit, pos);
        if (layer != PickedLayer.LARGE && layer != PickedLayer.MEDIUM && layer != PickedLayer.SMALL) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            int mode = slots.cycleSharedMode(34);
            if (mode < 0) {
                return InteractionResult.PASS;
            }
            host.syncSheetPillows();
            BedPlatePillowMode.tell(player, mode);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult cyclePlate2(
            Plate plate,
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            BedPlate2BlockEntity plate2) {
        BedPlateSheetPillowSlots slots = plate2.pillowSlots();
        PickedLayer layer =
                pick(plate, state, plate2.hasDuvet(), plate2.hasCover(), slots, hit, pos);
        if (layer != PickedLayer.LARGE && layer != PickedLayer.MEDIUM && layer != PickedLayer.SMALL) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            String next = plate2.cyclePillowPose();
            if (next == null) {
                return InteractionResult.PASS;
            }
            BedPlatePillowMode.tell(player, plate2.displayedPillowMode());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static PickedLayer pick(
            Plate plate,
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            BedPlateSheetPillowSlots slots,
            BlockHitResult hit,
            BlockPos pos) {
        return BedPlateSimpleBeddingShapes.pickLayer(
                plate,
                state,
                hasDuvet,
                hasCover,
                0,
                slots.mediumMat(),
                slots.smallMat(),
                slots,
                hit.getLocation(),
                pos);
    }

    private static boolean hostHasCover(BedPlateSheetPillowHost host) {
        if (host instanceof org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity plate3) {
            return plate3.hasCover();
        }
        if (host instanceof org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate4BlockEntity plate4) {
            return plate4.hasCover();
        }
        return false;
    }
}
