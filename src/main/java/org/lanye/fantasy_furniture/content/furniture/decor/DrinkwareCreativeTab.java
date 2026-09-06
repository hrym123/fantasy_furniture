package org.lanye.fantasy_furniture.content.furniture.decor;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.decor.item.DrinkwareBlockItem;

/** 创造栏：杯具九色各一格。 */
public final class DrinkwareCreativeTab {

    private DrinkwareCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= DrinkwareMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stack(mat)));
        }
    }

    public static ItemStack stack(int materialId) {
        return DrinkwareBlockItem.stackWithMaterial(ModBlocks.DRINKWARE.item().get(), materialId);
    }
}
