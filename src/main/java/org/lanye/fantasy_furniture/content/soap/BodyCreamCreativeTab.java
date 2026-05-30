package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.BodyCreamBlockItem;

/** 创造栏：六色乳霜各一条。 */
public final class BodyCreamCreativeTab {

    private BodyCreamCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= BodyCreamMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(mat)));
        }
    }

    public static ItemStack stackForMaterial(int materialId) {
        return BodyCreamBlockItem.stackWithMaterial(ModBlocks.BODY_CREAM.item().get(), materialId);
    }
}
