package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapMoldSoapProductGeoModel;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 模具内成品皂：{@code soap_bar} 方块 Geo，挂于 {@code soap_product} 锚点（非物品栏 GeoItem）。 */
@OnlyIn(Dist.CLIENT)
final class SoapMoldSoapProductRenderer {

    private static final SoapMoldEmbeddedSoapBarRenderer RENDERER = new SoapMoldEmbeddedSoapBarRenderer();

    private SoapMoldSoapProductRenderer() {}

    static void draw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            SoapMoldBlockEntity blockEntity) {
        if (blockEntity.contents().phase() != SoapMoldPhase.READY) {
            return;
        }
        // 锚点 = 盆心；soap_bar geo 管线原点在方块角点
        poseStack.translate(-0.5f, 0f, -0.5f);
        RENDERER.render(blockEntity, 0f, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }

    /**
     * 盆内嵌套渲染：锚点为盆心（{@code soap_product}），先换算为方块角点再套用
     * {@link GeoBlockRenderer} 的 {@code translate(0.5,0,0.5)}，与地上 {@code soap_bar} 一致。
     */
    private static final class SoapMoldEmbeddedSoapBarRenderer extends GeoBlockRenderer<SoapMoldBlockEntity> {

        SoapMoldEmbeddedSoapBarRenderer() {
            super(new SoapMoldSoapProductGeoModel());
        }

        @Override
        public void actuallyRender(
                PoseStack poseStack,
                SoapMoldBlockEntity animatable,
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
            if (!isReRender) {
                AnimationState<SoapMoldBlockEntity> animationState =
                        new AnimationState<>(animatable, 0, 0, partialTick, false);
                getGeoModel()
                        .handleAnimations(
                                animatable, getInstanceId(animatable), animationState);
                poseStack.translate(0.5f, 0f, 0.5f);
            }
            this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

            RenderType type =
                    renderType != null
                            ? renderType
                            : RenderType.entityCutoutNoCull(getTextureLocation(animatable));
            VertexConsumer consumer =
                    buffer != null ? buffer : bufferSource.getBuffer(type);
            for (GeoBone bone : model.topLevelBones()) {
                renderRecursively(
                        poseStack,
                        animatable,
                        bone,
                        type,
                        bufferSource,
                        consumer,
                        true,
                        partialTick,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha);
            }
        }
    }
}
