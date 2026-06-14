package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.common.client.config.ClientRenderTuning;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.common.state.BanquetteShape;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import org.lanye.reverie_core.util.ReveriePerfLog;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

/**
 * 卡座拐角：在 {@link ReverieGeoBlockRenderer#rotateBlock} 之后对拐角 geo 追加 Y 旋转；角度见
 * {@link org.lanye.fantasy_furniture.content.furniture.common.client.config.ClientRenderTuning.Banquette}。碰撞箱在
 * {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock} 中单独旋转以对齐模型，此处不改动。
 */
@OnlyIn(Dist.CLIENT)
public final class BanquetteGeoBlockRenderer extends ReverieGeoBlockRenderer<BanquetteBlockEntity> {

    public BanquetteGeoBlockRenderer(GeoModel<BanquetteBlockEntity> model) {
        super(model, GeoRenderTier.STATIC);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            BanquetteBlockEntity animatable,
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
            ReveriePerfLog.finish("render.geo.block.banquette", t0);
        }
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        super.rotateBlock(facing, poseStack);
        if (animatable == null) {
            return;
        }
        BanquetteShape shape = animatable.getBlockState().getValue(BanquetteBlock.SHAPE);
        if (shape == BanquetteShape.CORNER_LEFT) {
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(ClientRenderTuning.Banquette.CORNER_YAW_LEFT_DEG));
        } else if (shape == BanquetteShape.CORNER_RIGHT) {
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            ClientRenderTuning.Banquette.CORNER_YAW_LEFT_DEG
                                    + ClientRenderTuning.Banquette.CORNER_YAW_RIGHT_OFFSET_DEG));
        }
    }
}
