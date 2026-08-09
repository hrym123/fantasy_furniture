package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapRackBottleRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 架上瓶罐临时/方案 2 overlay：复用各瓶单件 geo + 颜料贴图。 */
public final class SoapRackBottleOverlayGeoModel extends GeoModel<SoapRackBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapRackBlockEntity animatable) {
        SoapBottleKind kind = SoapRackBottleRenderState.kind();
        if (kind == null) {
            return BodyWashAssets.singleModelLocation();
        }
        return switch (kind) {
            case BODY_WASH -> BodyWashAssets.singleModelLocation();
            case SHAMPOO -> ShampooAssets.singleModelLocation();
            case BODY_CREAM -> BodyCreamAssets.singleModelLocation();
        };
    }

    @Override
    public ResourceLocation getTextureResource(SoapRackBlockEntity animatable) {
        SoapBottleKind kind = SoapRackBottleRenderState.kind();
        int mat = SoapRackBottleRenderState.materialId();
        if (kind == null) {
            return new BodyWashAppearance(mat).textureLocation();
        }
        return switch (kind) {
            case BODY_WASH -> new BodyWashAppearance(mat).textureLocation();
            case SHAMPOO -> new ShampooAppearance(mat).textureLocation();
            case BODY_CREAM -> new BodyCreamAppearance(mat).textureLocation();
        };
    }

    @Override
    public ResourceLocation getAnimationResource(SoapRackBlockEntity animatable) {
        SoapBottleKind kind = SoapRackBottleRenderState.kind();
        if (kind == null) {
            return SoapRackAssets.ANIMATION;
        }
        return switch (kind) {
            case BODY_WASH -> BodyWashAssets.singleAnimationLocation();
            case SHAMPOO -> ShampooAssets.singleAnimationLocation();
            case BODY_CREAM -> BodyCreamAssets.singleAnimationLocation();
        };
    }
}
