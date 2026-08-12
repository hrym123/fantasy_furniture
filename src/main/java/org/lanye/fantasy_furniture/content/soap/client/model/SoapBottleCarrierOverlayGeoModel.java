package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleCarrierRenderState;
import software.bernie.geckolib.model.GeoModel;

/**
 * 瓶罐摞 carrier overlay：复用现有 {@code soap_rack} / {@code soap_box} 方块 geo。
 *
 * <p>TODO：正式应对齐 moonstarfish 组合目录导出（完成态 {@code 肥皂架}/{@code 肥皂盒}，中间态 {@code *2}；
 * 第 3 位乳霜用组合 {@code 乳霜.bbmodel}）。
 */
public final class SoapBottleCarrierOverlayGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        SoapStackCarrierKind kind = SoapBottleCarrierRenderState.kind();
        if (kind == SoapStackCarrierKind.BOX) {
            return new SoapBoxAppearance(SoapBottleCarrierRenderState.boxMaterialId())
                    .boxModelLocation(false);
        }
        return SoapRackAssets.MODEL;
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
