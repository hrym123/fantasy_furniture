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
 * 蓝色礼帽 Gecko 渲染器：GUI/手持/头戴共用同一实例。
 *
 * <p>玩家头戴 geo 由 {@link org.lanye.fantasy_furniture.client.BlueTopHatPlayerLayer} 在头部骨骼上调用
 * {@link ItemDisplayContext#HEAD} 绘制；原版 {@link net.minecraft.client.renderer.entity.layers.CustomHeadLayer} 画的是物品 JSON 模型，
 * 对本物品已由 {@link org.lanye.fantasy_furniture.mixin.CustomHeadLayerMixin} 取消，避免“扁平面片 + geo”叠两层。
 *
 * <p>对 {@link ItemDisplayContext#HEAD}：跳过 Gecko 地面用的 {@code translate(0.5, 0.51, 0.5)}，并做 Bedrock 与头戴矩阵的 X 轴 180° 与竖直微调。
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
        } else if (!isReRender) {
            poseStack.translate(0.5f, 0.51f, 0.5f);
        }
    }
}
