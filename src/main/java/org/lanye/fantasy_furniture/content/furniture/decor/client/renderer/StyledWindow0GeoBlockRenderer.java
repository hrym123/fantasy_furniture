package org.lanye.fantasy_furniture.content.furniture.decor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0Materials;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0SharedTextures;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0Shapes;
import org.lanye.fantasy_furniture.content.furniture.decor.block.StyledWindow0Block;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.StyledWindow0BlockEntity;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import org.lanye.reverie_core.util.ReveriePerfLog;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

/**
 * 0号窗户：仅绑定 geo / 纹理 / 静态动画资源；姿态与几何完全以资源文件为准，
 * 本类<strong>不</strong>覆盖 {@link ReverieGeoBlockRenderer} 的旋转逻辑。
 */
@OnlyIn(Dist.CLIENT)
public final class StyledWindow0GeoBlockRenderer extends ReverieGeoBlockRenderer<StyledWindow0BlockEntity> {

    /** 与 {@link StyledWindow0BlockEntity} 的 {@code PlayState.STOP} 一致；多造型共用，避免每 geo 一份空动画 JSON。 */
    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public StyledWindow0GeoBlockRenderer() {
        super(
                new GeoModel<StyledWindow0BlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(StyledWindow0BlockEntity entity) {
                        int shape = entity.getBlockState().getValue(StyledWindow0Block.SHAPE);
                        String b = StyledWindow0Shapes.geoBasename(shape);
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID, "geo/block/" + b + ".geo.json");
                    }

                    @Override
                    public ResourceLocation getTextureResource(StyledWindow0BlockEntity entity) {
                        int mat = StyledWindow0Block.materialIndex(entity.getBlockState());
                        return StyledWindow0SharedTextures.textureLocationForStem(
                                FantasyFurniture.MODID, StyledWindow0Materials.itemPreviewStem(mat));
                    }

                    @Override
                    public ResourceLocation getAnimationResource(StyledWindow0BlockEntity entity) {
                        return STATIC_ANIMATION;
                    }
                },
                GeoRenderTier.STATIC);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            StyledWindow0BlockEntity animatable,
            BakedGeoModel model,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        long t0 = ReveriePerfLog.start();
        try {
            super.actuallyRender(
                    poseStack,
                    animatable,
                    model,
                    renderType,
                    bufferSource,
                    buffer,
                    isReRender,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha);
        } finally {
            ReveriePerfLog.finish("render.geo.block.styled_window_0", t0);
        }
    }
}
