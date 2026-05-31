package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import software.bernie.geckolib.model.GeoModel;

/** 模具盆内成品皂：复用地上肥皂方块 geo（{@code soap_bar*.geo.json}）。 */
@OnlyIn(Dist.CLIENT)
public final class SoapMoldSoapProductGeoModel extends GeoModel<SoapMoldBlockEntity> {

    private SoapBarAppearance appearance(SoapMoldBlockEntity animatable) {
        if (animatable.contents().phase() == SoapMoldPhase.READY) {
            return animatable.pendingSoapAppearance();
        }
        return SoapBarAppearance.defaults();
    }

    @Override
    public ResourceLocation getModelResource(SoapMoldBlockEntity animatable) {
        return appearance(animatable).modelLocation();
    }

    @Override
    public ResourceLocation getTextureResource(SoapMoldBlockEntity animatable) {
        return appearance(animatable).textureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(SoapMoldBlockEntity animatable) {
        return appearance(animatable).animationLocation();
    }
}
