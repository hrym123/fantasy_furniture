package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 被套（六种材质，与床单色 1..6 对应）：仅能在已铺床单的 {@link ModBlocks#BED_PLATE6} 上使用。
 */
public final class BedPlate6DuvetCoverItem extends BedPlate6GeolibDecorItem {

    private final int materialId;

    public BedPlate6DuvetCoverItem(Properties properties, int materialId) {
        super(properties);
        if (!BedPlate6DuvetCoverMaterials.isValid(materialId)) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        this.materialId = materialId;
    }

    public int getMaterialId() {
        return materialId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return applyToBed(
                context.getLevel(),
                context.getClickedPos(),
                context.getLevel().getBlockState(context.getClickedPos()),
                player,
                context.getHand());
    }

    /**
     * 供 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#use} 在原版
     * {@link net.minecraft.world.level.block.BedBlock#use} 之前调用。
     */
    public static InteractionResult applyToBed(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        if (!state.is(ModBlocks.BED_PLATE6.block().get())) {
            return InteractionResult.PASS;
        }
        BlockPos footPos = footPos(state, pos);
        BlockEntity be = level.getBlockEntity(footPos);
        if (!(be instanceof BedPlate6BlockEntity plate)) {
            return InteractionResult.PASS;
        }
        if (!plate.hasDuvet()) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BedPlate6DuvetCoverItem held)) {
            return InteractionResult.PASS;
        }
        int materialId = held.getMaterialId();
        if (!level.isClientSide) {
            int onBed = plate.getCoverMaterialId();
            if (BedPlate6DuvetCoverMaterials.isValid(onBed)) {
                if (onBed == materialId) {
                    plate.setCoverMaterialId(0);
                    giveCover(player, stackForRegistryMaterial(materialId));
                } else {
                    plate.setCoverMaterialId(materialId);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    giveCover(player, stackForRegistryMaterial(onBed));
                }
            } else {
                plate.setCoverMaterialId(materialId);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void giveCover(Player player, ItemStack stack) {
        if (player == null || stack.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static ItemStack stackForRegistryMaterial(int id) {
        if (!BedPlate6DuvetCoverMaterials.isValid(id)) {
            return ItemStack.EMPTY;
        }
        ResourceLocation rl =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "bed_plate6_duvet_cover_" + id);
        if (!BuiltInRegistries.ITEM.containsKey(rl)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(rl));
    }

    private static BlockPos footPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }
}
