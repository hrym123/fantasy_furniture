package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.ShampooBlockItem;

/** 创造栏：六色洗发露各一条。 */
public final class ShampooCreativeTab {

    private ShampooCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= ShampooMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(mat)));
        }
    }

    public static ItemStack stackForMaterial(int materialId) {
        return ShampooBlockItem.stackWithMaterial(ModBlocks.SHAMPOO.item().get(), materialId);
    }
}
