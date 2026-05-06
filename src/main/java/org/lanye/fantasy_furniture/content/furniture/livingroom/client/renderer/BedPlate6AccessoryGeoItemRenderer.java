package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import org.lanye.fantasy_furniture.content.furniture.livingroom.client.model.BedPlate6AccessoryGeoItemModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6GeolibDecorItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 床板 6 床单 / 被套 / 枕头在 GUI 与手中的 GeckoLib 渲染。 */
public final class BedPlate6AccessoryGeoItemRenderer extends GeoItemRenderer<BedPlate6GeolibDecorItem> {

    public BedPlate6AccessoryGeoItemRenderer() {
        super(new BedPlate6AccessoryGeoItemModel());
    }
}
