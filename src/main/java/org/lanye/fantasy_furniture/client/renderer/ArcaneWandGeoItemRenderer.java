package org.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.client.model.ArcaneWandItemModel;
import org.lanye.fantasy_furniture.item.ArcaneWandItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 魔杖 Geo 物品双 Pass：主体 {@link RenderType#entityCutoutNoCull}，水晶壳 {@link RenderType#entityTranslucentCull}。
 * 骨骼名与 {@code arcane_wand.geo.json} 约定一致（见调查文档 2.1.1）。
 */
@OnlyIn(Dist.CLIENT)
public final class ArcaneWandGeoItemRenderer extends GeoItemRenderer<ArcaneWandItem> {

    /** 杖头金属/支架等走 cutout */
    public static final String BONE_HEAD_OPAQUE = "wand_head_opaque";

    /** 水晶等连续 alpha 走 translucent */
    public static final String BONE_SHELL = "shell";

    /** 杖身另一侧枝（与杖头水晶不同 Pass） */
    public static final String BONE_OTHER_BRANCH = "group10";

    public static final ArcaneWandGeoItemRenderer INSTANCE = new ArcaneWandGeoItemRenderer();

    private ArcaneWandGeoItemRenderer() {
        super(new ArcaneWandItemModel());
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            ArcaneWandItem animatable,
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
            AnimationState<ArcaneWandItem> animationState =
                    new AnimationState<>(animatable, 0, 0, partialTick, false);
            long instanceId = getInstanceId(animatable);
            GeoModel<ArcaneWandItem> currentModel = this.getGeoModel();
            animationState.setData(DataTickets.TICK, animatable.getTick(this.currentItemStack));
            animationState.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.renderPerspective);
            animationState.setData(DataTickets.ITEMSTACK, this.currentItemStack);
            animatable.getAnimatableInstanceCache()
                    .getManagerForId(instanceId)
                    .setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.renderPerspective);
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleAnimations(animatable, instanceId, animationState);
        }

        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        ResourceLocation tex = getTextureLocation(animatable);
        boolean foil = this.currentItemStack != null && this.currentItemStack.hasFoil();
        boolean gui = this.renderPerspective == ItemDisplayContext.GUI;

        RenderType cutout = RenderType.entityCutoutNoCull(tex);
        RenderType translucent = RenderType.entityTranslucentCull(tex);

        try {
            hideForOpaquePass(model);
            VertexConsumer cutoutBuf = ItemRenderer.getFoilBufferDirect(bufferSource, cutout, gui, foil);
            updateAnimatedTextureFrame(animatable);
            for (GeoBone root : model.topLevelBones()) {
                renderRecursively(
                        poseStack,
                        animatable,
                        root,
                        cutout,
                        bufferSource,
                        cutoutBuf,
                        isReRender,
                        partialTick,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha);
            }

            resetPassVisibility(model);

            hideForTranslucentPass(model);
            VertexConsumer transBuf = ItemRenderer.getFoilBufferDirect(bufferSource, translucent, gui, foil);
            updateAnimatedTextureFrame(animatable);
            for (GeoBone root : model.topLevelBones()) {
                renderRecursively(
                        poseStack,
                        animatable,
                        root,
                        translucent,
                        bufferSource,
                        transBuf,
                        isReRender,
                        partialTick,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha);
            }
        } finally {
            resetPassVisibility(model);
        }
    }

    private static void hideForOpaquePass(BakedGeoModel model) {
        model.getBone(BONE_SHELL).ifPresent(b -> b.setHidden(true));
    }

    private static void hideForTranslucentPass(BakedGeoModel model) {
        model.getBone(BONE_HEAD_OPAQUE).ifPresent(b -> b.setHidden(true));
        model.getBone(BONE_OTHER_BRANCH).ifPresent(b -> b.setHidden(true));
    }

    private static void resetPassVisibility(BakedGeoModel model) {
        model.getBone(BONE_SHELL).ifPresent(b -> b.setHidden(false));
        model.getBone(BONE_HEAD_OPAQUE).ifPresent(b -> b.setHidden(false));
        model.getBone(BONE_OTHER_BRANCH).ifPresent(b -> b.setHidden(false));
    }
}
