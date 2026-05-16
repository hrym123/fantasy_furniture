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
 * 床板 6 大号枕头：三种款式互斥，每款至多七种材质（部分款式+材质组合已从模组移除）；仅能在已铺床单的 {@link
 * ModBlocks#BED_PLATE6} 上放置。
 */
public final class BedPlate6LargePillowItem extends BedPlate6GeolibDecorItem {

    private final int styleId;
    private final int materialId;

    /**
     * 已从游戏内移除的大号枕头（款式 id × 床单色系 id）；读档遇此组合时卸下大号，{@link
     * org.lanye.fantasy_furniture.bootstrap.item.ModItems} 不再注册对应物品。
     */
    public static boolean isUnavailableLargeVariant(int styleId, int materialId) {
        return (styleId == 1 && materialId == 1) /* 条纹·奶油色 */
                || (styleId == 2 && materialId == 7) /* 纯色·可可棕 */
                || (styleId == 3 && materialId == 7); /* 格子·可可棕 */
    }

    public BedPlate6LargePillowItem(Properties properties, int styleId, int materialId) {
        super(properties);
        if (!BedPlate6LargePillowStyles.isValid(styleId)) {
            throw new IllegalArgumentException("styleId out of range: " + styleId);
        }
        if (!BedPlate6DuvetMaterials.isValid(materialId)) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        if (isUnavailableLargeVariant(styleId, materialId)) {
            throw new IllegalArgumentException("removed large pillow variant: " + styleId + "," + materialId);
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
        if (plate.hasLargePillow()) {
            /* 已有大号：替换/卸下改由拆卸手套 */
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            plate.setLargePillow(style, material);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static ItemStack stackForRegistry(int style, int material) {
        if (!BedPlate6LargePillowStyles.isValid(style)
                || !BedPlate6DuvetMaterials.isValid(material)
                || isUnavailableLargeVariant(style, material)) {
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
