package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 床品拆除：仅 {@link BedPlate6DisassemblyGloveItem} 主手交互，按叠放逆序卸下<strong>一层</strong>（小号 → 后中号 → 前中号 → 大号 → 被套 → 床单）。
 */
public final class BedPlate6BedDecorRemoval {

    private BedPlate6BedDecorRemoval() {}

    public static InteractionResult tryRemoveLastWithMainHandGlove(
            Level level,
            BlockState state,
            BlockPos pos,
            Player player,
            InteractionHand hand) {
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
        if (!level.isClientSide) {
            if (!popOneLayerServer(plate, player)) {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean popOneLayerServer(BedPlate6BlockEntity plate, Player player) {
        if (plate.hasSmallPillow()) {
            int m = plate.getSmallPillowMat();
            plate.setSmallPillowMat(0);
            give(player, BedPlate6SmallPillowItem.stackForRegistry(m));
            return true;
        }
        int mc = plate.getMediumPillowCount();
        if (mc == 2) {
            int a = plate.getMediumPillowMatFirst();
            int b = plate.getMediumPillowMatSecond();
            plate.setMediumPillowSlots(a, 0);
            give(player, BedPlate6MediumPillowItem.stackForRegistry(b));
            ensureSmallStillValidOrReturn(plate, player);
            return true;
        }
        if (mc == 1) {
            int a = plate.getMediumPillowMatFirst();
            plate.setMediumPillowSlots(0, 0);
            give(player, BedPlate6MediumPillowItem.stackForRegistry(a));
            ensureSmallStillValidOrReturn(plate, player);
            return true;
        }
        if (plate.hasLargePillow()) {
            int style = plate.getLargePillowStyleId();
            int mat = plate.getLargePillowMaterialId();
            plate.setLargePillow(0, 0);
            give(player, BedPlate6LargePillowItem.stackForRegistry(style, mat));
            return true;
        }
        if (plate.hasCover()) {
            int c = plate.getCoverMaterialId();
            plate.setCoverMaterialId(0);
            give(player, BedPlate6DuvetCoverItem.stackForRegistry(c));
            return true;
        }
        int d = plate.getDuvetMaterialId();
        plate.setDuvetMaterialId(0);
        give(player, BedPlate6DuvetItem.stackForRegistry(d));
        return true;
    }

    private static void ensureSmallStillValidOrReturn(BedPlate6BlockEntity plate, Player player) {
        if (plate.hasSmallPillow() && !plate.smallPillowCombinationValid()) {
            int sm = plate.getSmallPillowMat();
            plate.setSmallPillowMat(0);
            give(player, BedPlate6SmallPillowItem.stackForRegistry(sm));
        }
    }

    private static void give(Player player, ItemStack stack) {
        if (player == null || stack.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static BlockPos footPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }
}
