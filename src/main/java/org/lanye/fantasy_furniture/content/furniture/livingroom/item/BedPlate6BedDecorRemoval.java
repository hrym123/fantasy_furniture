package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DecorStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth.PickedDecorLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 床品拆除：仅 {@link BedPlate6DisassemblyGloveItem} 主手交互，按准心体素命中（与
 * {@link BedPlate6ComponentPick} / {@link BedPlate6PickShapesNorth#pickLayerByVoxelHit} 一致）卸下<strong>选中</strong>组件。
 */
public final class BedPlate6BedDecorRemoval {

    private BedPlate6BedDecorRemoval() {}

    public static InteractionResult tryRemoveSelectedWithMainHandGlove(
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem()
                instanceof BedPlate6DisassemblyGloveItem)) {
            return InteractionResult.PASS;
        }
        if (!state.is(ModBlocks.BED_PLATE6.block().get())) {
            return InteractionResult.PASS;
        }
        BlockPos footPos = footPos(state, pos);
        BlockEntity be = level.getBlockEntity(footPos);
        if (!(be instanceof BedPlate6BlockEntity plate)) {
            return InteractionResult.PASS;
        }
        if (!plate.hasDuvet()) {
            return InteractionResult.PASS;
        }
        double ly = (hit.getLocation().y - footPos.getY()) * 16.0;
        if (ly < 5.0) {
            return InteractionResult.PASS;
        }
        Direction towardHead = state.getValue(BedBlock.FACING);
        PickedDecorLayer layer =
                BedPlate6PickShapesNorth.pickLayerByVoxelHit(plate, hit.getLocation(), footPos, towardHead);
        if (layer == PickedDecorLayer.NONE) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (!popLayerServer(plate, player, layer)) {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean popLayerServer(
            BedPlate6BlockEntity plate, Player player, PickedDecorLayer layer) {
        return switch (layer) {
            case SMALL_PILLOW -> {
                if (!plate.hasSmallPillow()) {
                    yield false;
                }
                int m = plate.getSmallPillowMat();
                plate.setSmallPillowMat(0);
                BedPlate6DecorStorage.giveOrDropToPlayer(
                        player, BedPlate6SmallPillowItem.stackForRegistry(m));
                yield true;
            }
            case MEDIUM_REAR -> {
                if (plate.getMediumPillowCount() != 2) {
                    yield false;
                }
                int rear = plate.getMediumPillowMatFirst();
                int front = plate.getMediumPillowMatSecond();
                plate.setMediumPillowSlots(front, 0);
                BedPlate6DecorStorage.giveOrDropToPlayer(
                        player, BedPlate6MediumPillowItem.stackForRegistry(rear));
                returnInvalidatedDependents(plate, player);
                yield true;
            }
            case MEDIUM_FRONT -> {
                int mc = plate.getMediumPillowCount();
                if (mc == 2) {
                    int front = plate.getMediumPillowMatSecond();
                    plate.setMediumPillowSlots(plate.getMediumPillowMatFirst(), 0);
                    BedPlate6DecorStorage.giveOrDropToPlayer(
                            player, BedPlate6MediumPillowItem.stackForRegistry(front));
                    returnInvalidatedDependents(plate, player);
                    yield true;
                }
                if (mc == 1 && plate.hasLargePillow()) {
                    int a = plate.getMediumPillowMatFirst();
                    plate.setMediumPillowSlots(0, 0);
                    BedPlate6DecorStorage.giveOrDropToPlayer(
                            player, BedPlate6MediumPillowItem.stackForRegistry(a));
                    returnInvalidatedDependents(plate, player);
                    yield true;
                }
                yield false;
            }
            case MEDIUM_SOLO -> {
                if (plate.getMediumPillowCount() != 1 || plate.hasLargePillow()) {
                    yield false;
                }
                int a = plate.getMediumPillowMatFirst();
                plate.setMediumPillowSlots(0, 0);
                BedPlate6DecorStorage.giveOrDropToPlayer(
                        player, BedPlate6MediumPillowItem.stackForRegistry(a));
                returnInvalidatedDependents(plate, player);
                yield true;
            }
            case LARGE_PILLOW -> {
                if (!plate.hasLargePillow()) {
                    yield false;
                }
                int style = plate.getLargePillowStyleId();
                int mat = plate.getLargePillowMaterialId();
                plate.setLargePillow(0, 0);
                BedPlate6DecorStorage.giveOrDropToPlayer(
                        player, BedPlate6LargePillowItem.stackForRegistry(style, mat));
                returnInvalidatedDependents(plate, player);
                yield true;
            }
            case DUVET_COVER -> {
                if (!plate.hasCover()) {
                    yield false;
                }
                int c = plate.getCoverMaterialId();
                plate.setCoverMaterialId(0);
                BedPlate6DecorStorage.giveOrDropToPlayer(
                        player, BedPlate6DuvetCoverItem.stackForRegistry(c));
                yield true;
            }
            case DUVET -> {
                if (!plate.hasDuvet()) {
                    yield false;
                }
                BedPlate6DecorStorage.giveAllStoredDecorToPlayer(plate, player);
                yield true;
            }
            case NONE -> false;
        };
    }

    /**
     * 卸下某层后若组合不再合法，连带卸下并返还依赖件（当前主要为：失去大号或中号底枕后的小号枕）。
     */
    private static void returnInvalidatedDependents(BedPlate6BlockEntity plate, Player player) {
        if (plate.hasSmallPillow() && !plate.smallPillowCombinationValid()) {
            int sm = plate.getSmallPillowMat();
            plate.setSmallPillowMat(0);
            BedPlate6DecorStorage.giveOrDropToPlayer(
                    player, BedPlate6SmallPillowItem.stackForRegistry(sm));
        }
    }

    private static BlockPos footPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }
}
