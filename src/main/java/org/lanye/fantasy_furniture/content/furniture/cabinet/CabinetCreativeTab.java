package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.item.CabinetBlockItem;

/** 创造栏：柜子1型四色、柜子2型六色。 */
public final class CabinetCreativeTab {

    private CabinetCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        for (int m = 1; m <= CabinetMaterials.count(CabinetKind.CABINET_1); m++) {
            int mat = m;
            list.add(out -> out.accept(stackCabinet1(mat)));
        }
        for (int m = 1; m <= CabinetMaterials.count(CabinetKind.CABINET_2); m++) {
            int mat = m;
            list.add(out -> out.accept(stackCabinet2(mat)));
        }
    }

    public static ItemStack stackCabinet1(int materialId) {
        return CabinetBlockItem.stackWithMaterial(ModBlocks.CABINET_1.item().get(), materialId);
    }

    public static ItemStack stackCabinet2(int materialId) {
        return CabinetBlockItem.stackWithMaterial(ModBlocks.CABINET_2.item().get(), materialId);
    }
}
