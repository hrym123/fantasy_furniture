package org.lanye.fantasy_furniture.content.furniture.decor;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.decor.item.ComputerBlockItem;

/** 创造栏：电脑1型 / 2型各六色。 */
public final class ComputerCreativeTab {

    private ComputerCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= ComputerMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackComputer1(mat)));
        }
        for (int m = 1; m <= ComputerMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackComputer2(mat)));
        }
    }

    public static ItemStack stackComputer1(int materialId) {
        return ComputerBlockItem.stackWithMaterial(ModBlocks.COMPUTER_1.item().get(), materialId);
    }

    public static ItemStack stackComputer2(int materialId) {
        return ComputerBlockItem.stackWithMaterial(ModBlocks.COMPUTER_2.item().get(), materialId);
    }
}
