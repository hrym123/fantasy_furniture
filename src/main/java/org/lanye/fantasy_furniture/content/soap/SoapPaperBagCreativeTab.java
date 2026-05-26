package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBagBlockItem;

/** 创造栏：六色 + 彩色共七条。 */
public final class SoapPaperBagCreativeTab {

    private SoapPaperBagCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= SoapPaperBagMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(mat)));
        }
    }

    public static ItemStack stackForMaterial(int bagMaterialId) {
        return SoapPaperBagBlockItem.stackWithBagMaterial(
                ModBlocks.SOAP_PAPER_BAG.item().get(), bagMaterialId);
    }
}
