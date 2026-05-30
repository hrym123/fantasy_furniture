package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;

/** 创造栏：六色沐浴露各一条。 */
public final class BodyWashCreativeTab {

    private BodyWashCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= BodyWashMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(mat)));
        }
    }

    public static ItemStack stackForMaterial(int materialId) {
        return BodyWashBlockItem.stackWithMaterial(ModBlocks.BODY_WASH.item().get(), materialId);
    }
}
