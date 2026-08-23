package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;

/** 床板2型：破坏时散落共用寝具物品。 */
public final class BedPlate2DecorStorage {

    private BedPlate2DecorStorage() {}

    public static boolean hasStoredDecor(BedPlate2BlockEntity plate) {
        return plate.hasDuvet()
                || plate.hasCover()
                || plate.getLargePillowCount() > 0
                || plate.hasMediumPillow()
                || plate.hasSmallPillow();
    }

    public static List<ItemStack> collectStoredDecorStacks(BedPlate2BlockEntity plate) {
        List<ItemStack> stacks = new ArrayList<>();
        if (plate.hasDuvet()) {
            addIfNonEmpty(stacks, BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId()));
        }
        if (plate.hasCover()) {
            addIfNonEmpty(stacks, BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId()));
        }
        if (plate.hasLargePillowSlot(1)) {
            addIfNonEmpty(
                    stacks,
                    BedPlate6LargePillowItem.stackForRegistry(
                            plate.getLargePillowStyleId(1), plate.getLargePillowMaterialId(1)));
        }
        if (plate.hasLargePillowSlot(2)) {
            addIfNonEmpty(
                    stacks,
                    BedPlate6LargePillowItem.stackForRegistry(
                            plate.getLargePillowStyleId(2), plate.getLargePillowMaterialId(2)));
        }
        if (plate.hasMediumPillow()) {
            addIfNonEmpty(stacks, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMat()));
        }
        if (plate.hasSmallPillow()) {
            addIfNonEmpty(stacks, BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat()));
        }
        return stacks;
    }

    public static void clearAllStoredDecor(BedPlate2BlockEntity plate) {
        plate.clearAllBedding();
    }

    public static boolean spillAllAsWorldDrops(Level level, BlockPos dropAt, BedPlate2BlockEntity plate) {
        List<ItemStack> stacks = collectStoredDecorStacks(plate);
        if (stacks.isEmpty()) {
            return false;
        }
        clearAllStoredDecor(plate);
        for (ItemStack stack : stacks) {
            Block.popResource(level, dropAt, stack);
        }
        return true;
    }

    private static void addIfNonEmpty(List<ItemStack> stacks, ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }
}
