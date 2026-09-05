package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateBedFootPos;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSimpleBeddingStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.BedPlateSimpleBeddingClientPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/**
 * 床板3型：材质档 + 共用床单 / 被套。顺序：被套 → 床单 → 睡眠。
 *
 * <p>落地弹跳与摔落减免：仅已铺床单时启用。空床体碰撞见 {@link BedPlateEmptyBedCollision}。
 */
public final class BedPlate3Block extends BedPlateBlock {

    private static final ThreadLocal<Integer> PENDING_PLAYER_ON_REMOVE = new ThreadLocal<>();

    private final BedPlate3MaterialVariant variant;

    public BedPlate3Block(BlockBehaviour.Properties properties, BedPlate3MaterialVariant variant) {
        super(properties, BedPlate3BlockEntity::new);
        this.variant = variant;
    }

    public BedPlate3MaterialVariant variant() {
        return variant;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var be = level.getBlockEntity(bedFootWorldPos(state, pos));
        boolean hasDuvet = be instanceof BedPlate3BlockEntity plate && plate.hasDuvet();
        boolean hasCover = be instanceof BedPlate3BlockEntity plate && plate.hasCover();
        return BedPlateSimpleBeddingShapes.pickShapeFor(
                BedPlateSimpleBeddingShapes.Plate.PLATE3, state, hasDuvet, hasCover);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BedPlateSimpleBeddingShapes.bodyShape(BedPlateSimpleBeddingShapes.Plate.PLATE3, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level instanceof Level l) {
            return BedPlateSimpleBeddingClientPick.resolveCloneItemStack(l, state, pos);
        }
        return new ItemStack(this);
    }

    @Override
    protected boolean enablesSoftLanding(BlockGetter level, BlockState state, BlockPos pos) {
        var be = level.getBlockEntity(bedFootWorldPos(state, pos));
        return be instanceof BedPlate3BlockEntity plate && plate.hasDuvet();
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

    private void breakDropsForPlate(
            Level level, BlockState state, BlockPos anyPartPos, boolean creative, boolean onlyDropBedOnFootPart) {
        BlockPos foot = bedFootWorldPos(state, anyPartPos);
        BlockEntity be = level.getBlockEntity(foot);
        if (!(be instanceof BedPlate3BlockEntity plate)) {
            return;
        }
        boolean hasBedding = plate.hasDuvet() || plate.hasCover();
        if (hasBedding) {
            if (creative) {
                plate.clearBedding();
            } else {
                BedPlateSimpleBeddingStorage.spill(
                        level,
                        foot,
                        plate.hasDuvet(),
                        plate.getDuvetMaterialId(),
                        plate.hasCover(),
                        plate.getCoverMaterialId(),
                        plate::clearBedding);
            }
        }
        if (!creative
                && (!onlyDropBedOnFootPart
                        || state.getValue(BedBlock.PART)
                                == net.minecraft.world.level.block.state.properties.BedPart.FOOT)) {
            Block.popResource(level, foot, new ItemStack(this));
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
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetItem) {
            InteractionResult duvet = BedPlate6DuvetItem.applyToBed(level, pos, state, player, hand);
            if (duvet.consumesAction() || duvet == InteractionResult.FAIL) {
                return duvet;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
