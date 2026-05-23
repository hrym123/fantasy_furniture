package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;

/** 创造栏：六种颜料档肥皂（默认磨损），单物品 id + NBT。 */
public final class SoapBarCreativeTab {

    private SoapBarCreativeTab() {}

    public static void appendDefaultWearEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(mat)));
        }
    }

    public static ItemStack stackForMaterial(int materialId) {
        return SoapBarBlockItem.stackWithAppearance(
                ModBlocks.SOAP_BAR.item().get(),
                new SoapBarAppearance(SoapBarAppearance.DEFAULT_WEAR, materialId));
    }

    /** 六种颜料档（完整磨损），供展示地图等批量摆放。 */
    public static List<ItemStack> allDefaultWearStacks() {
        List<ItemStack> stacks = new ArrayList<>(SoapBarMaterials.COUNT);
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            stacks.add(stackForMaterial(m));
        }
        return List.copyOf(stacks);
    }
}
