package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 专用床单（七种材质之一）：仅能对 {@link ModBlocks#BED_PLATE6} 使用，不可放置为方块。
 */
public final class BedPlate6DuvetItem extends Item {

    private final int materialId;

    public BedPlate6DuvetItem(Properties properties, int materialId) {
        super(properties);
        if (!BedPlate6DuvetMaterials.isValid(materialId)) {
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
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BedPlate6DuvetItem held)) {
            return InteractionResult.PASS;
        }
        int materialId = held.getMaterialId();
        if (!level.isClientSide) {
            int onBed = plate.getDuvetMaterialId();
            if (BedPlate6DuvetMaterials.isValid(onBed)) {
                if (onBed == materialId) {
                    plate.setDuvetMaterialId(0);
                    giveDuvet(player, stackForRegistryMaterial(materialId));
                } else {
                    plate.setDuvetMaterialId(materialId);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    giveDuvet(player, stackForRegistryMaterial(onBed));
                }
            } else {
                plate.setDuvetMaterialId(materialId);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void giveDuvet(Player player, ItemStack stack) {
        if (player == null || stack.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static ItemStack stackForRegistryMaterial(int id) {
        if (!BedPlate6DuvetMaterials.isValid(id)) {
            return ItemStack.EMPTY;
        }
        ResourceLocation rl =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "bed_plate6_duvet_" + id);
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
