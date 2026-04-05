package org.example.lanye.fantasy_furniture.geolib.client;

import java.util.function.Function;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.model.GeoModel;
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

    /**
     * 共享同一套纹理、但 geo/animation 资源 basename 需按方块实体变化时使用（例如同一方块多种形态）。
     * 路径约定与 {@link #defaultGeoRendererProvider(String, String)} 相同，仅 basename 由 {@code basenameForEntity} 推导。
     *
     * @param textureBasename {@code textures/block/&lt;textureBasename&gt;.png}
     * @param basenameForEntity 返回 {@code geo/block/&lt;basename&gt;.geo.json} 与 {@code animations/block/&lt;basename&gt;.animation.json} 的 basename
     */
    public static <BE extends BlockEntity & GeoBlockEntity> BlockEntityRendererProvider<BE> variableBasenameGeoRendererProvider(
            String modid, String textureBasename, Function<BE, String> basenameForEntity) {
        return ctx ->
                new GeoBlockRenderer<>(
                        new GeoModel<BE>() {
                            @Override
                            public ResourceLocation getModelResource(BE entity) {
                                String b = basenameForEntity.apply(entity);
                                return ResourceLocation.fromNamespaceAndPath(modid, "geo/block/" + b + ".geo.json");
                            }

                            @Override
                            public ResourceLocation getTextureResource(BE entity) {
                                return ResourceLocation.fromNamespaceAndPath(
                                        modid, "textures/block/" + textureBasename + ".png");
                            }

                            @Override
                            public ResourceLocation getAnimationResource(BE entity) {
                                String b = basenameForEntity.apply(entity);
                                return ResourceLocation.fromNamespaceAndPath(
                                        modid, "animations/block/" + b + ".animation.json");
                            }
                        });
    }
}
