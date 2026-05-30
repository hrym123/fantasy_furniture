package org.lanye.fantasy_furniture.content.soap;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;

/** 创造栏：肥皂套系瓶罐 / 包装盒 / 模具（设计书主序未收录项，便于验收）。 */
public final class SoapSeriesCreativeTab {

    private SoapSeriesCreativeTab() {}

    public static void appendEntries(List<Consumer<CreativeModeTab.Output>> list) {
        BodyWashCreativeTab.appendEntries(list);
        list.add(out -> out.accept(ModBlocks.SHAMPOO.item().get()));
        BodyCreamCreativeTab.appendEntries(list);
        SoapPaperBoxCreativeTab.appendEntries(list);
        list.add(out -> out.accept(ModBlocks.SOAP_MOLD.item().get()));
    }
}
