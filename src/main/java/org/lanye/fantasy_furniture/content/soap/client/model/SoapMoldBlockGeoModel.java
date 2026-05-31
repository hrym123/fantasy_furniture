package org.lanye.fantasy_furniture.content.soap.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public final class SoapMoldBlockGeoModel extends GeoModel<SoapMoldBlockEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "geo/block/soap_mold.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "textures/block/soap_mold.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "animations/block/soap_mold.animation.json");

    @Override
    public ResourceLocation getModelResource(SoapMoldBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SoapMoldBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SoapMoldBlockEntity animatable) {
        return ANIMATION;
    }
}
