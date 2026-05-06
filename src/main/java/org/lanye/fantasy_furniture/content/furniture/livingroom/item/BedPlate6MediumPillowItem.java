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
 * <p>无床单则 {@link InteractionResult#FAIL}。仅一只时：手持同材质 → 收起；手持异材质 → 再铺一只（后槽），至多两只。
 * 双槽时优先匹配并卸下「后放」槽（slot1），再匹配 slot0；两槽材质均与手持不同则仅更换后槽材质并退还旧后槽物品。
 *
 * <p>{@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#use} 中顺序为：被套 → 大号枕头 →
 * <strong>中号枕头</strong> → 床单，避免床单先卸下后枕头仍尝试交互。
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
        if (!plate.hasDuvet()) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BedPlate6MediumPillowItem held)) {
            return InteractionResult.PASS;
        }
        int m = held.getMaterialId();
        if (!level.isClientSide) {
            applyServer(plate, player, stack, m);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void applyServer(BedPlate6BlockEntity plate, Player player, ItemStack stack, int m) {
        int a = plate.getMediumPillowMatFirst();
        int b = plate.getMediumPillowMatSecond();
        int count = plate.getMediumPillowCount();

        if (count == 0) {
            plate.setMediumPillowSlots(m, 0);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return;
        }
        if (count == 1) {
            if (a == m) {
                plate.setMediumPillowSlots(0, 0);
                givePillow(player, stackForRegistry(m));
            } else {
                plate.setMediumPillowSlots(a, m);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return;
        }
        // count == 2
        if (b == m) {
            plate.setMediumPillowSlots(a, 0);
            givePillow(player, stackForRegistry(m));
            return;
        }
        if (a == m) {
            plate.setMediumPillowSlots(b, 0);
            givePillow(player, stackForRegistry(m));
            return;
        }
        // 双槽且材质均不匹配：将「后槽」换为手持材质，退还旧后槽物品（前槽不动）。
        plate.setMediumPillowSlots(a, m);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        givePillow(player, stackForRegistry(b));
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
