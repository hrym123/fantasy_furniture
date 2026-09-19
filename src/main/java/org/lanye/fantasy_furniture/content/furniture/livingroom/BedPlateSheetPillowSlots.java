package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate3Block;
import org.lanye.reverie_core.geolib.bed.BedPlateSide;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;

/**
 * 床板1 / 3 / 4 型枕头槽。
 * 床单可选；大号至多两只，中号一只，小号一只（无被子也可落床面）。
 * 床板2/3/4 世界外形用各型号组合 geo，两只大号各占一组。
 */
public final class BedPlateSheetPillowSlots {

    private static final String NBT_LARGE1_STYLE = "Lg1Style";
    private static final String NBT_LARGE1_MAT = "Lg1Mat";
    private static final String NBT_LARGE2_STYLE = "Lg2Style";
    private static final String NBT_LARGE2_MAT = "Lg2Mat";
    private static final String NBT_MEDIUM = "MdPillow";
    private static final String NBT_SMALL = "SmPillow";
    private static final String NBT_LARGE1_FLAT = "Lg1Flat";
    private static final String NBT_LARGE2_FLAT = "Lg2Flat";
    private static final String NBT_MEDIUM_POSE = "MdPose";

    private int large1StyleId;
    private int large1MaterialId;
    private int large2StyleId;
    private int large2MaterialId;
    /** 大号平放。缺省竖放，与已放置的床一致。 */
    private boolean large1Flat;
    private boolean large2Flat;
    private int mediumMat;
    private int mediumSide = 1;
    /** -1 跟随同侧大号；0 平放；1 竖放。 */
    private int mediumPose = -1;
    private int smallMat;
    private int smallPlace = 4;

    public boolean hasLarge() {
        return hasLargeSlot(1) || hasLargeSlot(2);
    }

    public boolean hasLargeSlot(int index) {
        return index == 1 ? validLarge(large1StyleId, large1MaterialId) : validLarge(large2StyleId, large2MaterialId);
    }

    public int largeCount() {
        return (hasLargeSlot(1) ? 1 : 0) + (hasLargeSlot(2) ? 1 : 0);
    }

    /** 绘制用：优先第一只，没有则第二只。 */
    public int largeStyleId() {
        if (hasLargeSlot(1)) {
            return large1StyleId;
        }
        return hasLargeSlot(2) ? large2StyleId : 0;
    }

    public int largeMaterialId() {
        if (hasLargeSlot(1)) {
            return large1MaterialId;
        }
        return hasLargeSlot(2) ? large2MaterialId : 0;
    }

    public boolean hasMedium() {
        return BedPlate6MediumPillowMaterials.isValid(mediumMat);
    }

    public int mediumMat() {
        return hasMedium() ? mediumMat : 0;
    }

    public int mediumSide() {
        return mediumSide == 2 ? 2 : 1;
    }

    public int smallPlace() {
        return smallPlace >= 4 && smallPlace <= 7 ? smallPlace : 4;
    }

    public boolean hasSmall() {
        return BedPlate6SmallPillowMaterials.isValid(smallMat);
    }

    public int smallMat() {
        return hasSmall() ? smallMat : 0;
    }

    public boolean hasAny() {
        return hasLarge() || hasMedium() || hasSmall();
    }

    public boolean canAddLarge() {
        return largeCount() < 2;
    }

    public boolean tryAddLarge(int styleId, int materialId) {
        if (!canAddLarge() || !validLarge(styleId, materialId)) {
            return false;
        }
        if (!hasLargeSlot(1)) {
            return writeLargeSlot(1, styleId, materialId);
        }
        return writeLargeSlot(2, styleId, materialId);
    }

    /**
     * 床板1型：1=左列（组合组 S1），2=右列（组合组 S2）。该列已有大号则失败。
     * S1 在导出里靠 +X，默认 Geo 会做 X 镜像，画到床尾右锚点的 −X，也就是面朝与床正面相同方向时的左手边。
     */
    public boolean canAddLargeSide(int side) {
        return side == 2 ? !hasLargeSlot(2) : !hasLargeSlot(1);
    }

    public boolean tryAddLargeSide(int side, int styleId, int materialId) {
        if (!canAddLargeSide(side) || !validLarge(styleId, materialId)) {
            return false;
        }
        return writeLargeSlot(side == 2 ? 2 : 1, styleId, materialId);
    }

    public int largeStyleOnSide(int side) {
        return side == 2 ? (hasLargeSlot(2) ? large2StyleId : 0) : (hasLargeSlot(1) ? large1StyleId : 0);
    }

    public int largeMaterialOnSide(int side) {
        return side == 2
                ? (hasLargeSlot(2) ? large2MaterialId : 0)
                : (hasLargeSlot(1) ? large1MaterialId : 0);
    }

    /** 床板1型大号：true 平放（DAP），false 竖放（S）。 */
    public boolean largeFlat(int side) {
        if (!hasLargeSlot(side)) {
            return false;
        }
        return side == 2 ? large2Flat : large1Flat;
    }

    /** @return 切换后是否平放 */
    public boolean toggleLargeFlat(int side) {
        if (side == 2) {
            large2Flat = hasLargeSlot(2) && !large2Flat;
            return large2Flat;
        }
        large1Flat = hasLargeSlot(1) && !large1Flat;
        return large1Flat;
    }

    /** 中号：调试指定优先，否则同侧大号竖放时竖放、否则平放。 */
    public boolean mediumStanding() {
        if (!hasMedium()) {
            return false;
        }
        if (mediumPose == 0) {
            return false;
        }
        if (mediumPose == 1) {
            return true;
        }
        return hasLargeSlot(mediumSide()) && !largeFlat(mediumSide());
    }

    /** @return 切换后是否竖放 */
    public boolean toggleMediumStanding() {
        if (!hasMedium()) {
            return false;
        }
        boolean standing = !mediumStanding();
        mediumPose = standing ? 1 : 0;
        return standing;
    }

    public boolean canAddMedium() {
        return !hasMedium();
    }

    public boolean tryAddMedium(int materialId) {
        return tryAddMediumSide(1, materialId);
    }

    public boolean tryAddMediumSide(int side, int materialId) {
        if (!canAddMedium() || !BedPlate6MediumPillowMaterials.isValid(materialId)) {
            return false;
        }
        this.mediumMat = materialId;
        this.mediumSide = side == 2 ? 2 : 1;
        this.mediumPose = -1;
        return true;
    }

    public boolean canAddSmall() {
        return !hasSmall();
    }

    public boolean tryAddSmall(int materialId) {
        return tryAddSmallPlace(4, materialId);
    }

    public boolean tryAddSmallPlace(int place, int materialId) {
        if (!canAddSmall() || !BedPlate6SmallPillowMaterials.isValid(materialId)) {
            return false;
        }
        this.smallMat = materialId;
        this.smallPlace = place >= 4 && place <= 7 ? place : 4;
        return true;
    }

    public void clearLargeSlot(int side) {
        if (side == 2) {
            large2StyleId = 0;
            large2MaterialId = 0;
            large2Flat = false;
            return;
        }
        large1StyleId = 0;
        large1MaterialId = 0;
        large1Flat = false;
    }

    public void clearMedium() {
        mediumMat = 0;
        mediumSide = 1;
        mediumPose = -1;
    }

    public void clearSmall() {
        smallMat = 0;
        smallPlace = 4;
    }

    public void clear() {
        large1StyleId = 0;
        large1MaterialId = 0;
        large1Flat = false;
        large2StyleId = 0;
        large2MaterialId = 0;
        large2Flat = false;
        mediumMat = 0;
        mediumSide = 1;
        mediumPose = -1;
        smallMat = 0;
        smallPlace = 4;
    }

    public void write(CompoundTag tag) {
        writeLarge(tag, NBT_LARGE1_STYLE, NBT_LARGE1_MAT, large1StyleId, large1MaterialId);
        writeLarge(tag, NBT_LARGE2_STYLE, NBT_LARGE2_MAT, large2StyleId, large2MaterialId);
        if (large1Flat && validLarge(large1StyleId, large1MaterialId)) {
            tag.putBoolean(NBT_LARGE1_FLAT, true);
        }
        if (large2Flat && validLarge(large2StyleId, large2MaterialId)) {
            tag.putBoolean(NBT_LARGE2_FLAT, true);
        }
        if (mediumMat != 0) {
            tag.putInt(NBT_MEDIUM, mediumMat);
            tag.putInt("MdSide", mediumSide == 2 ? 2 : 1);
            if (mediumPose == 0 || mediumPose == 1) {
                tag.putInt(NBT_MEDIUM_POSE, mediumPose);
            }
        }
        if (smallMat != 0) {
            tag.putInt(NBT_SMALL, smallMat);
            tag.putInt("SmPlace", smallPlace);
        }
    }

    public void read(CompoundTag tag) {
        large1StyleId = tag.getInt(NBT_LARGE1_STYLE);
        large1MaterialId = tag.getInt(NBT_LARGE1_MAT);
        if (!validLarge(large1StyleId, large1MaterialId)) {
            large1StyleId = 0;
            large1MaterialId = 0;
            large1Flat = false;
        } else {
            large1Flat = tag.getBoolean(NBT_LARGE1_FLAT);
        }
        large2StyleId = tag.getInt(NBT_LARGE2_STYLE);
        large2MaterialId = tag.getInt(NBT_LARGE2_MAT);
        if (!validLarge(large2StyleId, large2MaterialId)) {
            large2StyleId = 0;
            large2MaterialId = 0;
            large2Flat = false;
        } else {
            large2Flat = tag.getBoolean(NBT_LARGE2_FLAT);
        }
        mediumMat = tag.getInt(NBT_MEDIUM);
        if (!BedPlate6MediumPillowMaterials.isValid(mediumMat)) {
            mediumMat = 0;
            mediumSide = 1;
            mediumPose = -1;
        } else {
            mediumSide = tag.getInt("MdSide") == 2 ? 2 : 1;
            int pose = tag.contains(NBT_MEDIUM_POSE) ? tag.getInt(NBT_MEDIUM_POSE) : -1;
            mediumPose = pose == 0 || pose == 1 ? pose : -1;
        }
        smallMat = tag.getInt(NBT_SMALL);
        if (!BedPlate6SmallPillowMaterials.isValid(smallMat)) {
            smallMat = 0;
            smallPlace = 4;
        } else {
            int place = tag.getInt("SmPlace");
            smallPlace = place >= 4 && place <= 7 ? place : 4;
        }
    }

    public void collect(List<ItemStack> stacks) {
        if (hasLargeSlot(1)) {
            add(stacks, BedPlate6LargePillowItem.stackForRegistry(large1StyleId, large1MaterialId));
        }
        if (hasLargeSlot(2)) {
            add(stacks, BedPlate6LargePillowItem.stackForRegistry(large2StyleId, large2MaterialId));
        }
        if (hasMedium()) {
            add(stacks, BedPlate6MediumPillowItem.stackForRegistry(mediumMat));
        }
        if (hasSmall()) {
            add(stacks, BedPlate6SmallPillowItem.stackForRegistry(smallMat));
        }
    }

    public List<ItemStack> collect() {
        List<ItemStack> stacks = new ArrayList<>();
        collect(stacks);
        return stacks;
    }

    public static InteractionResult applyLarge(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return apply(level, pos, state, player, hand, Kind.LARGE);
    }

    public static InteractionResult applyMedium(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return apply(level, pos, state, player, hand, Kind.MEDIUM);
    }

    public static InteractionResult applySmall(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return apply(level, pos, state, player, hand, Kind.SMALL);
    }

    private static InteractionResult apply(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, Kind kind) {
        BedPlateSheetPillowHost host = host(level, state, pos);
        if (host == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        int side = plate1Side(state);
        if (!level.isClientSide) {
            boolean added =
                    switch (kind) {
                        case LARGE -> {
                            if (!(stack.getItem() instanceof BedPlate6LargePillowItem held)) {
                                yield false;
                            }
                            BedPlateSheetPillowSlots slots = host.sheetPillows();
                            yield side == 0
                                    ? slots.tryAddLarge(held.getStyleId(), held.getMaterialId())
                                    : slots.tryAddLargeSide(side, held.getStyleId(), held.getMaterialId());
                        }
                        case MEDIUM -> {
                            if (!(stack.getItem() instanceof BedPlate6MediumPillowItem held)) {
                                yield false;
                            }
                            yield side == 0
                                    ? host.sheetPillows().tryAddMedium(held.getMaterialId())
                                    : host.sheetPillows().tryAddMediumSide(side, held.getMaterialId());
                        }
                        case SMALL -> {
                            if (!(stack.getItem() instanceof BedPlate6SmallPillowItem held)) {
                                yield false;
                            }
                            yield side == 0
                                    ? host.sheetPillows().tryAddSmall(held.getMaterialId())
                                    : host.sheetPillows()
                                            .tryAddSmallPlace(
                                                    smallPlaceFor(host.sheetPillows(), side),
                                                    held.getMaterialId());
                        }
                    };
            if (!added) {
                return InteractionResult.FAIL;
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            host.syncSheetPillows();
        } else if (!previewCanAdd(host, stack, kind, side)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean previewCanAdd(
            BedPlateSheetPillowHost host, ItemStack stack, Kind kind, int side) {
        BedPlateSheetPillowSlots slots = host.sheetPillows();
        return switch (kind) {
            case LARGE -> stack.getItem() instanceof BedPlate6LargePillowItem
                    && (side == 0 ? slots.canAddLarge() : slots.canAddLargeSide(side));
            case MEDIUM -> stack.getItem() instanceof BedPlate6MediumPillowItem && slots.canAddMedium();
            case SMALL -> stack.getItem() instanceof BedPlate6SmallPillowItem && slots.canAddSmall();
        };
    }

    /**
     * 0=不是床板1型；1=左列；2=右列。
     * 左右跟床的朝向走：面朝与床正面相同的方向时，左手边是左列。不要按“正对着床头看”来镜像。
     */
    private static int plate1Side(BlockState state) {
        if (!(state.getBlock() instanceof BedPlate1Block)) {
            return 0;
        }
        return state.getValue(BedPlate1Block.SIDE) == BedPlateSide.LEFT ? 1 : 2;
    }

    private static int smallPlaceFor(BedPlateSheetPillowSlots slots, int side) {
        if (side == 2) {
            return slots.hasLargeSlot(2) ? 7 : 5;
        }
        return slots.hasLargeSlot(1) ? 6 : 4;
    }

    private static BedPlateSheetPillowHost host(Level level, BlockState state, BlockPos pos) {
        BlockEntity be;
        if (state.getBlock() instanceof BedPlate1Block) {
            be = BedPlate1Block.decorEntity(level, state, pos);
        } else if (state.getBlock() instanceof BedPlate3Block
                || state.is(ModBlocks.BED_PLATE4.block().get())) {
            be = level.getBlockEntity(BedPlateBedFootPos.footPos(state, pos));
        } else {
            return null;
        }
        return be instanceof BedPlateSheetPillowHost found ? found : null;
    }

    private static boolean validLarge(int styleId, int materialId) {
        return BedPlate6LargePillowStyles.isValid(styleId)
                && BedPlate6DuvetMaterials.isValid(materialId)
                && !BedPlate6LargePillowItem.isUnavailableLargeVariant(styleId, materialId);
    }

    private boolean writeLargeSlot(int side, int styleId, int materialId) {
        if (side == 2) {
            large2StyleId = styleId;
            large2MaterialId = materialId;
            large2Flat = false;
        } else {
            large1StyleId = styleId;
            large1MaterialId = materialId;
            large1Flat = false;
        }
        return true;
    }

    private static void writeLarge(CompoundTag tag, String styleKey, String matKey, int styleId, int materialId) {
        if (styleId != 0) {
            tag.putInt(styleKey, styleId);
            tag.putInt(matKey, materialId);
        }
    }

    private static void add(List<ItemStack> stacks, ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }

    private enum Kind {
        LARGE,
        MEDIUM,
        SMALL
    }
}
