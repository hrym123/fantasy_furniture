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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DecorStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.PickedLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.Plate;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate4BlockEntity;

/**
 * 床板 2/3/4：主手拆卸手套按准心卸选中的那一件床品。对准木架则不处理，也不睡觉。
 */
public final class BedPlateSimpleGloveRemoval {

    private BedPlateSimpleGloveRemoval() {}

    public static InteractionResult tryRemove(
            Plate plate,
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND
                || !(player.getItemInHand(hand).getItem() instanceof BedPlate6DisassemblyGloveItem)) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(BedPlateBedFootPos.footPos(state, pos));
        if (be instanceof BedPlate2BlockEntity plate2) {
            return removePlate2(plate, level, state, pos, player, hit, plate2);
        }
        if (be instanceof BedPlate3BlockEntity plate3) {
            return removeSlots(
                    plate,
                    level,
                    state,
                    pos,
                    player,
                    hit,
                    plate3.hasDuvet(),
                    plate3.hasCover(),
                    plate3.getDuvetMaterialId(),
                    plate3.getCoverMaterialId(),
                    plate3.sheetPillows(),
                    plate3::setDuvetMaterialId,
                    plate3::setCoverMaterialId,
                    plate3::syncSheetPillows);
        }
        if (be instanceof BedPlate4BlockEntity plate4) {
            return removeSlots(
                    plate,
                    level,
                    state,
                    pos,
                    player,
                    hit,
                    plate4.hasDuvet(),
                    plate4.hasCover(),
                    plate4.getDuvetMaterialId(),
                    plate4.getCoverMaterialId(),
                    plate4.sheetPillows(),
                    plate4::setDuvetMaterialId,
                    plate4::setCoverMaterialId,
                    plate4::syncSheetPillows);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult removeSlots(
            Plate plate,
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            boolean hasDuvet,
            boolean hasCover,
            int duvetMat,
            int coverMat,
            BedPlateSheetPillowSlots slots,
            java.util.function.IntConsumer setDuvet,
            java.util.function.IntConsumer setCover,
            Runnable syncPillows) {
        if (!hasDuvet && !hasCover && !slots.hasAny()) {
            return InteractionResult.PASS;
        }
        PickedLayer layer =
                BedPlateSimpleBeddingShapes.pickLayer(
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
        if (layer == PickedLayer.BODY) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide
                && !popSlots(
                        plate, state, pos, player, hit, slots, layer, hasDuvet, hasCover, duvetMat, coverMat, setDuvet, setCover, syncPillows)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean popSlots(
            Plate plate,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            BedPlateSheetPillowSlots slots,
            PickedLayer layer,
            boolean hasDuvet,
            boolean hasCover,
            int duvetMat,
            int coverMat,
            java.util.function.IntConsumer setDuvet,
            java.util.function.IntConsumer setCover,
            Runnable syncPillows) {
        return switch (layer) {
            case LARGE -> {
                int side = BedPlateSimpleBeddingShapes.largeSlotAt(plate, state, slots, hit.getLocation(), pos);
                if (!slots.hasLargeSlot(side)) {
                    yield false;
                }
                int style = slots.largeStyleOnSide(side);
                int mat = slots.largeMaterialOnSide(side);
                slots.clearLargeSlot(side);
                syncPillows.run();
                give(player, BedPlate6LargePillowItem.stackForRegistry(style, mat));
                yield true;
            }
            case MEDIUM -> {
                if (!slots.hasMedium()) {
                    yield false;
                }
                int mat = slots.mediumMat();
                slots.clearMedium();
                syncPillows.run();
                give(player, BedPlate6MediumPillowItem.stackForRegistry(mat));
                yield true;
            }
            case SMALL -> {
                if (!slots.hasSmall()) {
                    yield false;
                }
                int mat = slots.smallMat();
                slots.clearSmall();
                syncPillows.run();
                give(player, BedPlate6SmallPillowItem.stackForRegistry(mat));
                yield true;
            }
            case DUVET_COVER -> {
                if (!hasCover) {
                    yield false;
                }
                setCover.accept(0);
                give(player, BedPlate6DuvetCoverItem.stackForRegistry(coverMat));
                yield true;
            }
            case DUVET -> {
                if (!hasDuvet) {
                    yield false;
                }
                setDuvet.accept(0);
                give(player, BedPlate6DuvetItem.stackForRegistry(duvetMat));
                if (hasCover) {
                    give(player, BedPlate6DuvetCoverItem.stackForRegistry(coverMat));
                }
                yield true;
            }
            case BODY -> false;
        };
    }

    private static InteractionResult removePlate2(
            Plate plate,
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            BedPlate2BlockEntity plate2) {
        BedPlateSheetPillowSlots slots = plate2.pillowSlots();
        if (!plate2.hasDuvet() && !plate2.hasCover() && !slots.hasAny()) {
            return InteractionResult.PASS;
        }
        PickedLayer layer =
                BedPlateSimpleBeddingShapes.pickLayer(
                        plate,
                        state,
                        plate2.hasDuvet(),
                        plate2.hasCover(),
                        0,
                        plate2.getMediumPillowMat(),
                        plate2.getSmallPillowMat(),
                        slots,
                        hit.getLocation(),
                        pos);
        if (layer == PickedLayer.BODY) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && !popPlate2(plate, state, pos, player, hit, plate2, slots, layer)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean popPlate2(
            Plate plate,
            BlockState state,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            BedPlate2BlockEntity plate2,
            BedPlateSheetPillowSlots slots,
            PickedLayer layer) {
        return switch (layer) {
            case LARGE -> {
                int side = BedPlateSimpleBeddingShapes.largeSlotAt(plate, state, slots, hit.getLocation(), pos);
                if (plate2.getLargePillowCount() == 1) {
                    side = plate2.hasLargePillowSlot(1) ? 1 : 2;
                } else if (!plate2.hasLargePillowSlot(side)) {
                    yield false;
                }
                int style = plate2.getLargePillowStyleId(side);
                int mat = plate2.getLargePillowMaterialId(side);
                plate2.clearLargePillowSlot(side);
                give(player, BedPlate6LargePillowItem.stackForRegistry(style, mat));
                if (plate2.getLargePillowCount() == 0 && plate2.hasSmallPillow()) {
                    int small = plate2.getSmallPillowMat();
                    plate2.clearSmallPillow();
                    give(player, BedPlate6SmallPillowItem.stackForRegistry(small));
                }
                yield true;
            }
            case MEDIUM -> {
                if (!plate2.hasMediumPillow()) {
                    yield false;
                }
                int mat = plate2.getMediumPillowMat();
                int small = plate2.hasSmallPillow() ? plate2.getSmallPillowMat() : 0;
                plate2.clearMediumPillow();
                if (small != 0) {
                    plate2.clearSmallPillow();
                }
                give(player, BedPlate6MediumPillowItem.stackForRegistry(mat));
                if (small != 0) {
                    give(player, BedPlate6SmallPillowItem.stackForRegistry(small));
                }
                yield true;
            }
            case SMALL -> {
                if (!plate2.hasSmallPillow()) {
                    yield false;
                }
                int mat = plate2.getSmallPillowMat();
                plate2.clearSmallPillow();
                give(player, BedPlate6SmallPillowItem.stackForRegistry(mat));
                yield true;
            }
            case DUVET_COVER -> {
                if (!plate2.hasCover()) {
                    yield false;
                }
                int cover = plate2.getCoverMaterialId();
                plate2.setCoverMaterialId(0);
                give(player, BedPlate6DuvetCoverItem.stackForRegistry(cover));
                yield true;
            }
            case DUVET -> {
                if (!plate2.hasDuvet()) {
                    yield false;
                }
                int duvet = plate2.getDuvetMaterialId();
                int cover = plate2.hasCover() ? plate2.getCoverMaterialId() : 0;
                plate2.setDuvetMaterialId(0);
                give(player, BedPlate6DuvetItem.stackForRegistry(duvet));
                if (cover != 0) {
                    give(player, BedPlate6DuvetCoverItem.stackForRegistry(cover));
                }
                yield true;
            }
            case BODY -> false;
        };
    }

    private static void give(Player player, net.minecraft.world.item.ItemStack stack) {
        BedPlate6DecorStorage.giveOrDropToPlayer(player, stack);
    }
}
