package org.lanye.fantasy_furniture.client.renderer;

import org.lanye.fantasy_furniture.client.model.GeolibBlockItemModel;
import org.lanye.fantasy_furniture.geolib.GeolibBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** GeckoLib 方块物品统一渲染器（调试埋点已移除）。 */
public final class GeolibBlockItemRenderer extends GeoItemRenderer<GeolibBlockItem> {
    public GeolibBlockItemRenderer() {
        super(new GeolibBlockItemModel());
    }
}
