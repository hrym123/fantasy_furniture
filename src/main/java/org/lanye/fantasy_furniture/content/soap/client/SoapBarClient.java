package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

@OnlyIn(Dist.CLIENT)
public final class SoapBarClient {

    private SoapBarClient() {}

    public static BlockEntityRendererProvider<SoapBarBlockEntity> blockRendererProvider() {
        return ctx ->
                new GeoBlockRenderer<>(
                        new GeoModel<SoapBarBlockEntity>() {
                            private SoapBarAppearance appearance(SoapBarBlockEntity entity) {
                                return SoapBarAppearance.fromState(entity.getBlockState());
                            }

                            @Override
                            public ResourceLocation getModelResource(SoapBarBlockEntity entity) {
                                return appearance(entity).modelLocation();
                            }

                            @Override
                            public ResourceLocation getTextureResource(SoapBarBlockEntity entity) {
                                return appearance(entity).textureLocation();
                            }

                            @Override
                            public ResourceLocation getAnimationResource(SoapBarBlockEntity entity) {
                                return appearance(entity).animationLocation();
                            }
                        });
    }
}
