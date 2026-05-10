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
 * 床板 6 中号枕头：须先有床单。有大号时<strong>至多一只</strong>中号（大中 / 大中小）；无大号时可放两只（中中 / 中中小）。槽位顺序「先放 → 后放」。
 *
 * <p><strong>交互</strong>：右键仅<strong>放置</strong>（空槽放第一只；无大号且已有一只则再放第二只；已两只或大+中已满则不再消耗）。
 * 拆除改由主手 {@link BedPlate6DisassemblyGloveItem}。
 *
 * <p>无床单则 {@link InteractionResult#FAIL}。
 *
 * <p>{@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#use} 中顺序为：拆卸手套 → 被套 → 大号枕头 →
 * <strong>中号枕头</strong> → 床单。
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
        if (!level.isClientSide) {
            applyServerPlaceOnly(plate, player, stack, m);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 仅放置第二只；已满两只时不替换。 */
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
            if (plate.hasLargePillow()) {
                /* 大中：有大号时仅允许一只中号 */
                return;
            }
            plate.setMediumPillowSlots(a, m);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return;
        }
        /* count == 2：不替换已有枕头，不消耗物品 */
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
