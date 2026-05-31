package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.content.soap.item.SoapFlatLiquidItem;

/** 创造栏：沐浴液 / 洗发液六色各一条。 */
public final class SoapFlatLiquidCreativeTab {

    private SoapFlatLiquidCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(ModItems.BODY_WASH_LIQUID.get(), "body_wash_liquid", mat)));
        }
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(ModItems.SHAMPOO_LIQUID.get(), "shampoo_liquid", mat)));
        }
    }

    private static ItemStack stackForMaterial(net.minecraft.world.item.Item item, String stem, int materialId) {
        return SoapFlatLiquidItem.stackWithMaterial(item, stem, materialId);
    }
}
