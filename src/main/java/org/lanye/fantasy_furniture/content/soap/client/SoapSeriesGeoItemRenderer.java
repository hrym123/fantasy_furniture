package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapSeriesGeoItemModel;
import org.lanye.fantasy_furniture.content.soap.item.SoapSeriesBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapSeriesGeoItemRenderer extends GeoItemRenderer<SoapSeriesBlockItem> {

    public SoapSeriesGeoItemRenderer() {
        super(new SoapSeriesGeoItemModel());
    }
}
