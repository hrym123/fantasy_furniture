package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;

/** 床板3/4：破坏时散落共用床单 / 被套。 */
public final class BedPlateSimpleBeddingStorage {

    private BedPlateSimpleBeddingStorage() {}

    public static boolean spill(
            Level level,
            BlockPos dropAt,
            boolean hasDuvet,
            int duvetMat,
            boolean hasCover,
            int coverMat,
            Runnable clear) {
        List<ItemStack> stacks = new ArrayList<>();
        if (hasDuvet) {
            ItemStack duvet = BedPlate6DuvetItem.stackForRegistry(duvetMat);
            if (!duvet.isEmpty()) {
                stacks.add(duvet);
            }
        }
        if (hasCover) {
            ItemStack cover = BedPlate6DuvetCoverItem.stackForRegistry(coverMat);
            if (!cover.isEmpty()) {
                stacks.add(cover);
            }
        }
        if (stacks.isEmpty()) {
            return false;
        }
        clear.run();
        for (ItemStack stack : stacks) {
            Block.popResource(level, dropAt, stack);
        }
        return true;
    }
}
