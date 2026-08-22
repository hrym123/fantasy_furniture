package org.lanye.fantasy_furniture.content.furniture.decor.series;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import org.lanye.reverie_core.util.ReveriePerfLog;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public final class StyledWindowSeriesGeoBlockRenderer
        extends ReverieGeoBlockRenderer<StyledWindowSeriesBlockEntity> {

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public StyledWindowSeriesGeoBlockRenderer() {
        super(
                new GeoModel<StyledWindowSeriesBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(StyledWindowSeriesBlockEntity entity) {
                        StyledWindowSeriesSpec spec =
                                StyledWindowSeriesCatalog.get(entity.seriesId());
                        int shape =
                                Mth.clamp(
                                        entity.getBlockState().getValue(StyledWindowSeriesBlock.SHAPE),
                                        0,
                                        spec.shapeCount() - 1);
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "geo/block/" + spec.geoBasename(shape) + ".geo.json");
                    }

                    @Override
                    public ResourceLocation getTextureResource(StyledWindowSeriesBlockEntity entity) {
                        StyledWindowSeriesBlock block =
                                (StyledWindowSeriesBlock) entity.getBlockState().getBlock();
                        String stem = block.spec().textureStem(block.colorIndex());
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID, "textures/block/" + stem + ".png");
                    }

                    @Override
                    public ResourceLocation getAnimationResource(StyledWindowSeriesBlockEntity entity) {
                        return STATIC_ANIMATION;
                    }
                },
                GeoRenderTier.STATIC);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            StyledWindowSeriesBlockEntity animatable,
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
        poseStack.pushPose();
        try {
            StyledWindowSeriesSpec spec = StyledWindowSeriesCatalog.get(animatable.seriesId());
            int ou = spec.geoOffsetU();
            int ov = spec.geoOffsetV();
            if (ou != 0 || ov != 0) {
                Direction facing = animatable.getBlockState().getValue(StyledWindowSeriesBlock.FACING);
                Direction right = facing.getClockWise();
                // 北向模型空间：+X = 东；渲染前按朝向把「右×ou、上×ov」格偏移转到世界
                double dx = right.getStepX() * ou;
                double dz = right.getStepZ() * ou;
                double dy = ov;
                poseStack.translate(dx, dy, dz);
            }
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
            poseStack.popPose();
            ReveriePerfLog.finish(
                    "render.geo.block." + animatable.seriesId().idPrefix(), t0);
        }
    }
}
