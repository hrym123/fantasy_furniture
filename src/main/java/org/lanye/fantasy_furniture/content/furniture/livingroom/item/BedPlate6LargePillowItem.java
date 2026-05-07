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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6PillowPalette;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 大号枕头：三种款式互斥，每款七种材质；仅能在已铺床单的 {@link ModBlocks#BED_PLATE6} 上放置。
 */
public final class BedPlate6LargePillowItem extends BedPlate6GeolibDecorItem {

    private final int styleId;
    private final int materialId;

    public BedPlate6LargePillowItem(Properties properties, int styleId, int materialId) {
        super(properties);
        if (!BedPlate6LargePillowStyles.isValid(styleId)) {
            throw new IllegalArgumentException("styleId out of range: " + styleId);
        }
        if (!BedPlate6DuvetMaterials.isValid(materialId)) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        this.styleId = styleId;
        this.materialId = materialId;
    }

    public int getStyleId() {
        return styleId;
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
        if (!plate.hasLargePillow() && plate.getMediumPillowCount() == 2) {
            /* 合法组合不含「两只中号再叠大号」 */
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BedPlate6LargePillowItem held)) {
            return InteractionResult.PASS;
        }
        int style = held.getStyleId();
        int material = held.getMaterialId();
        if (!level.isClientSide) {
            if (plate.hasLargePillow()) {
                int onStyle = plate.getLargePillowStyleId();
                int onMat = plate.getLargePillowMaterialId();
                if (onStyle == style && onMat == material) {
                    if (plate.hasSmallPillow()) {
                        int sm = plate.getSmallPillowMat();
                        givePillow(player, BedPlate6SmallPillowItem.stackForRegistry(sm));
                        plate.setSmallPillowMat(0);
                    }
                    plate.setLargePillow(0, 0);
                    givePillow(player, stackForRegistry(style, material));
                } else if (onStyle == style) {
                    plate.setLargePillow(style, material);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    givePillow(player, stackForRegistry(style, onMat));
                } else {
                    plate.setLargePillow(style, material);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    givePillow(player, stackForRegistry(onStyle, onMat));
                }
            } else {
                plate.setLargePillow(style, material);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void givePillow(Player player, ItemStack stack) {
        if (player == null || stack.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    static ItemStack stackForRegistry(int style, int material) {
        if (!BedPlate6LargePillowStyles.isValid(style) || !BedPlate6DuvetMaterials.isValid(material)) {
            return ItemStack.EMPTY;
        }
        String path = "bed_plate6_pillow_large_"
                + BedPlate6LargePillowStyles.resourceSlug(style)
                + "_"
                + BedPlate6PillowPalette.colorSlug(material);
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, path);
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
