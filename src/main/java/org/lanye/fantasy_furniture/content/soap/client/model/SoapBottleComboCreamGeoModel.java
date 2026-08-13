package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBottleComboAssets;
import org.lanye.fantasy_furniture.content.soap.SoapRackAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapBottleComboCreamRenderState;
import software.bernie.geckolib.model.GeoModel;

/** 完成态第 3 位乳霜：组合目录 {@code 乳霜.bbmodel} 导出 geo。 */
public final class SoapBottleComboCreamGeoModel extends GeoModel<SoapBottleBlockEntity> {

    @Override
    public ResourceLocation getModelResource(SoapBottleBlockEntity animatable) {
        return SoapBottleComboAssets.CREAM_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SoapBottleBlockEntity animatable) {
        return new BodyCreamAppearance(SoapBottleComboCreamRenderState.materialId()).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapBottleBlockEntity animatable) {
        return SoapRackAssets.ANIMATION;
    }
}
