package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;

/** 床板1型：破坏时散落共用床单 / 被套等寝具。 */
public final class BedPlate1DecorStorage {

    private BedPlate1DecorStorage() {}

    public static boolean hasStoredDecor(BedPlate1BlockEntity plate) {
        return plate.hasDuvet() || plate.hasCover();
    }

    public static List<ItemStack> collectStoredDecorStacks(BedPlate1BlockEntity plate) {
        List<ItemStack> stacks = new ArrayList<>();
        if (plate.hasCover()) {
            ItemStack cover = BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId());
            if (!cover.isEmpty()) {
                stacks.add(cover);
            }
        }
        if (plate.hasDuvet()) {
            ItemStack duvet = BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId());
            if (!duvet.isEmpty()) {
                stacks.add(duvet);
            }
        }
        return stacks;
    }

    public static void clearAllStoredDecor(BedPlate1BlockEntity plate) {
        plate.clearDuvet();
    }

    public static boolean spillAllAsWorldDrops(Level level, BlockPos dropAt, BedPlate1BlockEntity plate) {
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
}
