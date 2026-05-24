package org.lanye.fantasy_furniture.content.debug.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.content.debug.DevelopmentMode;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 对齐探针方块物品；仅开发模式开启时可放置。 */
public final class GeolibAlignmentProbeBlockItem extends GeolibBlockItem {

    public GeolibAlignmentProbeBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!DevelopmentMode.enabled()) {
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }
}
