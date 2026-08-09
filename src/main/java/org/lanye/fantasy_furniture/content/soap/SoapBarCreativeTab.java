package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;

/** 创造栏：六种颜料档肥皂（满耐久），单物品 id + NBT。 */
public final class SoapBarCreativeTab {

    private SoapBarCreativeTab() {}

    public static void appendFullDurabilityEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            int mat = m;
            list.add(out -> out.accept(stackForMaterial(mat)));
        }
    }

    public static ItemStack stackForMaterial(int materialId) {
        ItemStack stack =
                SoapBarBlockItem.stackWithAppearance(
                        ModBlocks.SOAP_BAR.item().get(),
                        new SoapBarAppearance(SoapBarAppearance.DEFAULT_DURABILITY, materialId));
        SoapBarAppearance.writeCreativeParticle(stack);
        return stack;
    }

    /** 六种颜料档（满耐久），供展示地图等批量摆放。 */
    public static List<ItemStack> allFullDurabilityStacks() {
        List<ItemStack> stacks = new ArrayList<>(SoapBarMaterials.COUNT);
        for (int m = 1; m <= SoapBarMaterials.COUNT; m++) {
            stacks.add(stackForMaterial(m));
        }
        return List.copyOf(stacks);
    }
}
