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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateBedFootPos;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 共用床单（七种材质之一）：可铺在床板1 / 2 / 6 型上；世界外形按床型选 geo，不可放置为方块。
 * 物品栏 / 手持为单材质图（{@code textures/item/bed_plate6_duvet_*}）。
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
        if (state.getBlock() instanceof BedPlate1Block) {
            BedPlate1BlockEntity plate = BedPlate1Block.decorEntity(level, state, pos);
            if (plate == null) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BedPlate6DuvetItem held)) {
                return InteractionResult.PASS;
            }
            if (!BedPlate6DuvetMaterials.isSupportedOnBedPlate1(held.getMaterialId())) {
                /* 奶油色：板1 无专用贴图，禁止铺上（见 Opt-021） */
                return InteractionResult.FAIL;
            }
            if (!plate.canAddDuvet()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                plate.setDuvetMaterialId(held.getMaterialId());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (state.is(ModBlocks.BED_PLATE2.block().get())) {
            BlockPos footPos = BedPlateBedFootPos.footPos(state, pos);
            BlockEntity be = level.getBlockEntity(footPos);
            if (!(be instanceof BedPlate2BlockEntity plate)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BedPlate6DuvetItem held)) {
                return InteractionResult.PASS;
            }
            if (!plate.canAddDuvet()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                plate.setDuvetMaterialId(held.getMaterialId());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
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
        int onBed = plate.getDuvetMaterialId();
        if (BedPlate6DuvetMaterials.isValid(onBed)) {
            /* 已铺床单：替换/卸下改由拆卸手套，避免误触睡觉 */
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            plate.setDuvetMaterialId(materialId);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static ItemStack stackForRegistry(int id) {
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
        return BedPlateBedFootPos.footPos(state, pos);
    }
}
