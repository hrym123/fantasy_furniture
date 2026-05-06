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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 中号枕头：至多两只，槽位顺序为「先放的 → 后放的」；须先有床单。与大号枕头可共存。
 *
 * <p><strong>交互</strong>：非潜行右键仅<strong>放置</strong>（空槽放第一只；已有一只且手持<strong>相同</strong>材质则再放第二只，允许两槽同材质；
 * 已有一只且手持<strong>不同</strong>材质则放第二只异色；已两只则<strong>不再替换</strong>任一槽，手持任意中号均不消耗、不改变）。
 * 潜行右键<strong>拆除</strong>一只，顺序为后放 → 先放（LIFO）；手持中号枕头时由 {@link #applyToBed} 处理，空手或其它物品时由
 * {@link #trySneakRemoveFromBedWhenNotHoldingMedium} 在床方块交互中优先处理。
 *
 * <p>无床单则 {@link InteractionResult#FAIL}（仅在手握中号枕头且非潜行拆除路径时）。
 *
 * <p>{@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#use} 中顺序为：被套 → 大号枕头 →
 * 潜行拆中号（空手等）→ <strong>中号枕头</strong> → 床单。
 */
public final class BedPlate6MediumPillowItem extends BedPlate6GeolibDecorItem {

    private final int materialId;

    public BedPlate6MediumPillowItem(Properties properties, int materialId) {
        super(properties);
        if (!BedPlate6MediumPillowMaterials.isValid(materialId)) {
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
     * 潜行、且主手未拿中号枕头时：从床上拆下一只中号枕头（后槽优先）。供 {@link
     * org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#use} 在手持其它物品或空手时调用。
     */
    public static InteractionResult trySneakRemoveFromBedWhenNotHoldingMedium(
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
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6MediumPillowItem) {
            return InteractionResult.PASS;
        }
        if (plate.getMediumPillowCount() == 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            removeOneServer(plate, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 供 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#use} 调用。 */
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
        if (!(stack.getItem() instanceof BedPlate6MediumPillowItem held)) {
            return InteractionResult.PASS;
        }
        if (!plate.hasDuvet()) {
            return InteractionResult.FAIL;
        }
        int m = held.getMaterialId();
        if (player.isShiftKeyDown()) {
            if (plate.getMediumPillowCount() == 0) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                removeOneServer(plate, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            applyServerPlaceOnly(plate, player, stack, m);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 拆一只：先卸后槽，再卸前槽。 */
    private static void removeOneServer(BedPlate6BlockEntity plate, Player player) {
        int a = plate.getMediumPillowMatFirst();
        int b = plate.getMediumPillowMatSecond();
        int count = plate.getMediumPillowCount();
        if (count == 2) {
            plate.setMediumPillowSlots(a, 0);
            givePillow(player, stackForRegistry(b));
            return;
        }
        if (count == 1) {
            plate.setMediumPillowSlots(0, 0);
            givePillow(player, stackForRegistry(a));
        }
    }

    /** 非潜行：仅放置第二只；已满两只时不替换。 */
    private static void applyServerPlaceOnly(BedPlate6BlockEntity plate, Player player, ItemStack stack, int m) {
        int a = plate.getMediumPillowMatFirst();
        int count = plate.getMediumPillowCount();

        if (count == 0) {
            plate.setMediumPillowSlots(m, 0);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return;
        }
        if (count == 1) {
            plate.setMediumPillowSlots(a, m);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return;
        }
        /* count == 2：不替换已有枕头，不消耗物品 */
    }

    private static void givePillow(Player player, ItemStack give) {
        if (player == null || give.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(give)) {
            player.drop(give, false);
        }
    }

    static ItemStack stackForRegistry(int materialId) {
        if (!BedPlate6MediumPillowMaterials.isValid(materialId)) {
            return ItemStack.EMPTY;
        }
        ResourceLocation rl =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "bed_plate6_pillow_medium_" + materialId);
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
