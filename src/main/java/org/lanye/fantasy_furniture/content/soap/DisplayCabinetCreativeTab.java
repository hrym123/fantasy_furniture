package org.lanye.fantasy_furniture.content.soap;



import java.util.List;

import java.util.function.Consumer;

import net.minecraft.world.item.CreativeModeTab;

import net.minecraft.world.item.ItemStack;

import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;

import org.lanye.fantasy_furniture.content.soap.item.DisplayCabinetBlockItem;



/** 创造栏：六色陈列柜各一条。 */

public final class DisplayCabinetCreativeTab {



    private DisplayCabinetCreativeTab() {}



    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {

        for (int m = 1; m <= SoapPaperBoxMaterials.COUNT; m++) {

            int mat = m;

            list.add(out -> out.accept(stackForMaterial(mat)));

        }

    }



    public static ItemStack stackForMaterial(int materialId) {

        return DisplayCabinetBlockItem.stackWithMaterial(

                ModBlocks.DISPLAY_CABINET.item().get(), materialId);

    }

}

