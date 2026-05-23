package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;

/** 创造栏：关盖空盒六色，单物品 id + NBT。 */
public final class SoapBoxCreativeTab {

    private SoapBoxCreativeTab() {}

    public static void appendClosedEmptyBoxEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForBoxMaterial(mat)));
        }
    }

    public static ItemStack stackForBoxMaterial(int boxMaterialId) {
        return SoapBoxBlockItem.stackWithBoxMaterial(
                ModBlocks.SOAP_BOX.item().get(), boxMaterialId);
    }
}
