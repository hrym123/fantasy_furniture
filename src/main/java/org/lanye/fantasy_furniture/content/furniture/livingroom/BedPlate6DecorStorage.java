package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;

/**
 * 床板 6 寝具「存储」：由 {@link BedPlate6BlockEntity} 材质字段表示，破坏时散落为物品实体，拆卸手套卸下时进入背包或落在脚边。
 */
public final class BedPlate6DecorStorage {

    private BedPlate6DecorStorage() {}

    public static boolean hasStoredDecor(BedPlate6BlockEntity plate) {
        return plate.hasDuvet();
    }

    /** 按叠放自底向上收集当前床品物品（不改变 BE）。 */
    public static List<ItemStack> collectStoredDecorStacks(BedPlate6BlockEntity plate) {
        List<ItemStack> stacks = new ArrayList<>();
        if (!plate.hasDuvet()) {
            return stacks;
        }
        addIfNonEmpty(stacks, BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId()));
        if (plate.hasCover()) {
            addIfNonEmpty(stacks, BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId()));
        }
        if (plate.hasLargePillow()) {
            addIfNonEmpty(
                    stacks,
                    BedPlate6LargePillowItem.stackForRegistry(
                            plate.getLargePillowStyleId(), plate.getLargePillowMaterialId()));
        }
        if (plate.getMediumPillowCount() >= 1) {
            addIfNonEmpty(
                    stacks, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()));
        }
        if (plate.getMediumPillowCount() == 2) {
            addIfNonEmpty(
                    stacks, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatSecond()));
        }
        if (plate.hasSmallPillow()) {
            addIfNonEmpty(stacks, BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat()));
        }
        return stacks;
    }

    /** 清空 BE 中全部寝具层（与 {@link BedPlate6BlockEntity#setDuvetMaterialId(int)} 卸被单一致）。 */
    public static void clearAllStoredDecor(BedPlate6BlockEntity plate) {
        if (plate.hasDuvet()) {
            plate.setDuvetMaterialId(0);
        }
    }

    /** 将当前床上全部床品返还玩家（先收集再清空 BE）。 */
    public static void giveAllStoredDecorToPlayer(BedPlate6BlockEntity plate, Player player) {
        List<ItemStack> stacks = collectStoredDecorStacks(plate);
        clearAllStoredDecor(plate);
        for (ItemStack stack : stacks) {
            giveOrDropToPlayer(player, stack);
        }
    }

    /**
     * 方块被破坏等：在床尾格位置散落全部存储床品。
     *
     * @return 是否散落过至少一件
     */
    public static boolean spillAllAsWorldDrops(
            Level level, BlockPos dropAt, BedPlate6BlockEntity plate) {
        List<ItemStack> stacks = collectStoredDecorStacks(plate);
        if (stacks.isEmpty()) {
            return false;
        }
        clearAllStoredDecor(plate);
        for (ItemStack stack : stacks) {
            popAt(level, dropAt, stack);
        }
        return true;
    }

    /** 拆卸手套等：优先放入玩家背包，满则落在玩家脚下。 */
    public static void giveOrDropToPlayer(Player player, ItemStack stack) {
        if (player == null || stack.isEmpty()) {
            return;
        }
        ItemStack give = stack.copy();
        if (!player.getInventory().add(give)) {
            player.drop(give, false);
        }
    }

    private static void addIfNonEmpty(List<ItemStack> stacks, ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }

    private static void popAt(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            Block.popResource(level, pos, stack);
        }
    }
}
