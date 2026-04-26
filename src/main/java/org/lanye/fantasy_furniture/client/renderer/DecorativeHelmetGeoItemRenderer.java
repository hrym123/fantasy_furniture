package org.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.client.config.ClientRenderTuning;
import org.lanye.fantasy_furniture.client.model.DecorativeHelmetGeoModel;
import org.lanye.fantasy_furniture.item.DecorativeHelmetItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 装饰头饰 Gecko 渲染器：所有 {@link org.lanye.fantasy_furniture.item.DecorativeHelmetItem} 共用此实例。
 *
 * <p>玩家头戴 geo 由 {@link org.lanye.fantasy_furniture.client.DecorativeHelmetPlayerLayer} 在头部骨骼上调用
 * {@link ItemDisplayContext#HEAD} 绘制；原版 {@link net.minecraft.client.renderer.entity.layers.CustomHeadLayer} 画的是物品 JSON 模型，
 * 对本物品已由 {@link org.lanye.fantasy_furniture.mixin.CustomHeadLayerMixin} 取消，避免“扁平面片 + geo”叠两层。
 *
 * <p>对 {@link ItemDisplayContext#HEAD}：跳过 Gecko 地面用的 {@code translate(0.5, 0.51, 0.5)}，并做 Bedrock 与头戴矩阵的 X/Y 轴纠偏、左右镜像缩放与竖直微调。
 */
@OnlyIn(Dist.CLIENT)
public final class DecorativeHelmetGeoItemRenderer extends GeoItemRenderer<DecorativeHelmetItem> {

    public static final DecorativeHelmetGeoItemRenderer INSTANCE = new DecorativeHelmetGeoItemRenderer();

    private DecorativeHelmetGeoItemRenderer() {
        super(new DecorativeHelmetGeoModel());
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            DecorativeHelmetItem animatable,
            BakedGeoModel model,
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
        this.itemRenderTranslations = new Matrix4f(poseStack.last().pose());
        scaleModelForRender(
                this.scaleWidth,
                this.scaleHeight,
                poseStack,
                animatable,
                model,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay);
        if (!isReRender && this.renderPerspective == ItemDisplayContext.HEAD) {
            poseStack.mulPose(
                    Axis.XP.rotationDegrees(ClientRenderTuning.DecorativeHelmet.HEAD_ORBIT_X_DEG));
            poseStack.translate(0f, ClientRenderTuning.DecorativeHelmet.HEAD_NUDGE_Y, 0f);
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(ClientRenderTuning.DecorativeHelmet.HEAD_ORBIT_Y_DEG));
            poseStack.scale(
                    ClientRenderTuning.DecorativeHelmet.HEAD_MIRROR_X_SCALE,
                    1f,
                    1f);
        } else if (!isReRender) {
            poseStack.translate(
                    ClientRenderTuning.DecorativeHelmet.ITEM_GROUND_PIVOT_X,
                    ClientRenderTuning.DecorativeHelmet.ITEM_GROUND_PIVOT_Y,
                    ClientRenderTuning.DecorativeHelmet.ITEM_GROUND_PIVOT_Z);
        }
    }
}
