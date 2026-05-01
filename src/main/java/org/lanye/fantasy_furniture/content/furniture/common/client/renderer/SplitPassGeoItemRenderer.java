package org.lanye.fantasy_furniture.content.furniture.common.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.core.geolib.GeolibHandheldItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Geo 物品双 Pass 基类：
 * 主体走 cutout，指定 shell 骨骼走 translucent。
 *
 * <p>API 约定：子类仅需声明哪些骨骼属于 shell。渲染器会在第一遍隐藏 shell 几何，
 * 第二遍仅保留 shell 几何（不含立方体的枢轴骨会保留，用于维持动画层级变换）。
 */
public abstract class SplitPassGeoItemRenderer<T extends GeolibHandheldItem> extends GeoItemRenderer<T> {
    protected SplitPassGeoItemRenderer(GeoModel<T> model) {
        super(model);
    }

    /** 返回需走半透明 Pass 的骨骼名集合（与 geo.json 骨骼名一致，大小写敏感）。 */
    protected abstract Set<String> shellBoneNames();

    /** 主体 Pass 的 RenderType。 */
    protected RenderType opaqueRenderType(ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(texture);
    }

    /** shell Pass 的 RenderType。 */
    protected RenderType shellRenderType(ResourceLocation texture) {
        return RenderType.entityTranslucentCull(texture);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            T animatable,
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
            AnimationState<T> animationState = new AnimationState<>(animatable, 0, 0, partialTick, false);
            long instanceId = getInstanceId(animatable);
            GeoModel<T> currentModel = this.getGeoModel();
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

        ResourceLocation texture = getTextureLocation(animatable);
        boolean foil = this.currentItemStack != null && this.currentItemStack.hasFoil();
        boolean gui = this.renderPerspective == ItemDisplayContext.GUI;

        RenderType opaqueType = opaqueRenderType(texture);
        RenderType shellType = shellRenderType(texture);
        List<GeoBone> allBones = flattenBones(model);

        try {
            Map<GeoBone, Boolean> oldHidden = applyPassVisibility(allBones, shellBoneNames(), false);
            VertexConsumer opaqueBuffer = ItemRenderer.getFoilBufferDirect(bufferSource, opaqueType, gui, foil);
            renderPass(
                    poseStack,
                    animatable,
                    model,
                    opaqueType,
                    bufferSource,
                    opaqueBuffer,
                    isReRender,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha);
            restoreHidden(oldHidden);

            oldHidden = applyPassVisibility(allBones, shellBoneNames(), true);
            VertexConsumer shellBuffer = ItemRenderer.getFoilBufferDirect(bufferSource, shellType, gui, foil);
            renderPass(
                    poseStack,
                    animatable,
                    model,
                    shellType,
                    bufferSource,
                    shellBuffer,
                    isReRender,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    red,
                    green,
                    blue,
                    alpha);
            restoreHidden(oldHidden);
        } finally {
            // 兜底复位，避免单例渲染器状态泄漏到下一帧/下一物品。
            for (GeoBone bone : allBones) {
                bone.setHidden(false);
            }
        }
    }

    private void renderPass(
            PoseStack poseStack,
            T animatable,
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
        updateAnimatedTextureFrame(animatable);
        for (GeoBone root : model.topLevelBones()) {
            renderRecursively(
                    poseStack,
                    animatable,
                    root,
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
        }
    }

    private static List<GeoBone> flattenBones(BakedGeoModel model) {
        List<GeoBone> result = new ArrayList<>();
        for (GeoBone top : model.topLevelBones()) {
            collectBones(top, result);
        }
        return result;
    }

    private static void collectBones(GeoBone current, List<GeoBone> out) {
        out.add(current);
        for (GeoBone child : current.getChildBones()) {
            collectBones(child, out);
        }
    }

    private static Map<GeoBone, Boolean> applyPassVisibility(
            List<GeoBone> allBones, Set<String> shellBones, boolean shellPass) {
        Map<GeoBone, Boolean> oldHidden = new HashMap<>(allBones.size());
        for (GeoBone bone : allBones) {
            oldHidden.put(bone, bone.isHidden());
            // 仅对包含几何的骨骼做 pass 过滤；纯枢轴骨保持可见以保留层级变换。
            if (!bone.getCubes().isEmpty()) {
                boolean isShell = shellBones.contains(bone.getName());
                bone.setHidden(shellPass ? !isShell : isShell);
            }
        }
        return oldHidden;
    }

    private static void restoreHidden(Map<GeoBone, Boolean> oldHidden) {
        oldHidden.forEach(GeoBone::setHidden);
    }

    private static int countVisibleCubeBones(List<GeoBone> allBones) {
        int count = 0;
        for (GeoBone bone : allBones) {
            if (!bone.isHidden() && !bone.getCubes().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static int countShellCubeBones(List<GeoBone> allBones, Set<String> shellBones) {
        int count = 0;
        for (GeoBone bone : allBones) {
            if (!bone.getCubes().isEmpty() && shellBones.contains(bone.getName())) {
                count++;
            }
        }
        return count;
    }
}
