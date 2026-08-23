package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate2DecorStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateBedFootPos;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/**
 * 床板2型：共用寝具；枕头驱动拼装 1–4 Geo。顺序：被套 → 大号 → 中号 → 小号 → 被单 → 睡眠。
 */
public final class BedPlate2Block extends BedPlateBlock {

    private static final ThreadLocal<Integer> PENDING_PLAYER_ON_REMOVE = new ThreadLocal<>();

    public BedPlate2Block(
            BlockBehaviour.Properties properties,
            BlockEntityType.BlockEntitySupplier<? extends BedPlateBaseBlockEntity> entitySupplier) {
        super(properties, entitySupplier);
    }

    public static BlockPos bedFootWorldPos(BlockState state, BlockPos anyPartPos) {
        return BedPlateBedFootPos.footPos(state, anyPartPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            PENDING_PLAYER_ON_REMOVE.set(2);
            breakDropsForPlate(level, state, pos, player.getAbilities().instabuild, false);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            Integer pending = PENDING_PLAYER_ON_REMOVE.get();
            if (pending != null) {
                if (pending <= 1) {
                    PENDING_PLAYER_ON_REMOVE.remove();
                } else {
                    PENDING_PLAYER_ON_REMOVE.set(pending - 1);
                }
            } else {
                breakDropsForPlate(level, state, pos, false, true);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static void breakDropsForPlate(
            Level level, BlockState state, BlockPos anyPartPos, boolean creative, boolean onlyDropBedOnFootPart) {
        BlockPos foot = bedFootWorldPos(state, anyPartPos);
        BlockEntity be = level.getBlockEntity(foot);
        if (!(be instanceof BedPlate2BlockEntity plate)) {
            return;
        }
        if (BedPlate2DecorStorage.hasStoredDecor(plate)) {
            if (creative) {
                BedPlate2DecorStorage.clearAllStoredDecor(plate);
            } else {
                BedPlate2DecorStorage.spillAllAsWorldDrops(level, foot, plate);
            }
        }
        if (!creative
                && (!onlyDropBedOnFootPart || state.getValue(BedBlock.PART) == net.minecraft.world.level.block.state.properties.BedPart.FOOT)) {
            Block.popResource(level, foot, new ItemStack(ModBlocks.BED_PLATE2.item().get()));
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetCoverItem) {
            InteractionResult cover = BedPlate6DuvetCoverItem.applyToBed(level, pos, state, player, hand);
            if (cover != InteractionResult.PASS) {
                return cover;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6LargePillowItem) {
            InteractionResult pillow = BedPlate6LargePillowItem.applyToBed(level, pos, state, player, hand);
            if (pillow != InteractionResult.PASS) {
                return pillow;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6MediumPillowItem) {
            InteractionResult medium = BedPlate6MediumPillowItem.applyToBed(level, pos, state, player, hand);
            if (medium != InteractionResult.PASS) {
                return medium;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6SmallPillowItem) {
            InteractionResult small = BedPlate6SmallPillowItem.applyToBed(level, pos, state, player, hand);
            if (small != InteractionResult.PASS) {
                return small;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetItem) {
            InteractionResult duvet = BedPlate6DuvetItem.applyToBed(level, pos, state, player, hand);
            if (duvet.consumesAction()) {
                return duvet;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
