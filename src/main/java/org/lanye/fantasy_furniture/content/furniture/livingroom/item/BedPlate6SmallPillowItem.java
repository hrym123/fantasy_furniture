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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6SmallPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 小号枕头：仅当 {@link BedPlate6BlockEntity#canAddSmallPillow()} 为真时可放置并消耗（底枕为「大+一中」或「二中」，即大中小 / 中中小）。
 */
public final class BedPlate6SmallPillowItem extends BedPlate6GeolibDecorItem {

    private final int materialId;

    public BedPlate6SmallPillowItem(Properties properties, int materialId) {
        super(properties);
        if (!BedPlate6SmallPillowMaterials.isValid(materialId)) {
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
     * 潜行、且未拿小号枕头时：从床上卸下小号。须在潜行拆中号之前调用，以便先拆顶枕。
     */
    public static InteractionResult trySneakRemoveFromBedWhenNotHoldingSmall(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
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
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6SmallPillowItem) {
            return InteractionResult.PASS;
        }
        if (!plate.hasSmallPillow()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            int m = plate.getSmallPillowMat();
            plate.setSmallPillowMat(0);
            givePillow(player, stackForRegistry(m));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

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
        if (!(stack.getItem() instanceof BedPlate6SmallPillowItem held)) {
            return InteractionResult.PASS;
        }
        if (!plate.hasDuvet()) {
            return InteractionResult.FAIL;
        }
        int m = held.getMaterialId();
        if (player.isShiftKeyDown()) {
            if (!plate.hasSmallPillow()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                int on = plate.getSmallPillowMat();
                plate.setSmallPillowMat(0);
                givePillow(player, stackForRegistry(on));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            applyServerPlaceOnly(plate, player, stack, m);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void applyServerPlaceOnly(BedPlate6BlockEntity plate, Player player, ItemStack stack, int m) {
        if (plate.hasSmallPillow()) {
            return;
        }
        if (!plate.canAddSmallPillow()) {
            return;
        }
        plate.setSmallPillowMat(m);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void givePillow(Player player, ItemStack give) {
        if (player == null || give.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(give)) {
            player.drop(give, false);
        }
    }

    public static ItemStack stackForRegistry(int materialId) {
        if (!BedPlate6SmallPillowMaterials.isValid(materialId)) {
            return ItemStack.EMPTY;
        }
        ResourceLocation rl =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "bed_plate6_pillow_small_" + materialId);
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
