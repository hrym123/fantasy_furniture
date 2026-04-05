package org.example.lanye.fantasy_furniture.geolib.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * GeckoLib 动画方块在客户端的默认模型与渲染器：无额外逻辑时使用
 * {@link DefaultedBlockGeoModel} + {@link GeoBlockRenderer}，避免为每个方块各建薄子类。
 */
@OnlyIn(Dist.CLIENT)
public final class GeolibAnimatedBlockRenderers {

    private GeolibAnimatedBlockRenderers() {}

    /**
     * 与 {@link DefaultedBlockGeoModel} 约定一致：{@code modelBase} 为 {@code <modid>:<basename>}，
     * 对应 {@code geo/block/<basename>.geo.json}、{@code animations/block/<basename>.animation.json}、
     * {@code textures/block/<basename>.png}。
     */
    public static <BE extends BlockEntity & GeoBlockEntity> BlockEntityRendererProvider<BE> defaultGeoRendererProvider(
            ResourceLocation modelBase) {
        return ctx -> new GeoBlockRenderer<>(new DefaultedBlockGeoModel<>(modelBase));
    }

    public static <BE extends BlockEntity & GeoBlockEntity> BlockEntityRendererProvider<BE> defaultGeoRendererProvider(
            String modid, String basename) {
        return defaultGeoRendererProvider(ResourceLocation.fromNamespaceAndPath(modid, basename));
    }
}
