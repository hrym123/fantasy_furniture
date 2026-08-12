package org.lanye.fantasy_furniture.content.soap.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackCarrierInsert;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 肥皂架物品：对瓶罐摞右击插入载体（特殊场合 2/3）。 */
public final class SoapRackBlockItem extends GeolibBlockItem {

    public SoapRackBlockItem(Block block, Item.Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult stack = SoapBottleStackCarrierInsert.useOnBottleStack(context);
        if (stack != null) {
            return stack;
        }
        return super.useOn(context);
    }
}
