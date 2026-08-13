package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBottleComboAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import software.bernie.geckolib.model.GeoModel;

/**
 * 瓶罐摞 carrier overlay：组合目录导出 geo（完成态 / 中间态 {@code *2}）；贴图复用架/盒既有资源。
 */
public final class SoapBottleCarrierOverlayGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        SoapStackCarrierKind kind = SoapBottleCarrierRenderState.kind();
        boolean intermediate = SoapBottleCarrierRenderState.intermediate();
        if (kind == SoapStackCarrierKind.BOX) {
            return SoapBottleComboAssets.boxModel(intermediate);
        }
        return SoapBottleComboAssets.rackModel(intermediate);
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        SoapStackCarrierKind kind = SoapBottleCarrierRenderState.kind();
        if (kind == SoapStackCarrierKind.BOX) {
            return new SoapBoxAppearance(SoapBottleCarrierRenderState.boxMaterialId())
                    .boxTextureLocation(false);
        }
        return SoapRackAssets.TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return SoapRackAssets.ANIMATION;
    }
}
