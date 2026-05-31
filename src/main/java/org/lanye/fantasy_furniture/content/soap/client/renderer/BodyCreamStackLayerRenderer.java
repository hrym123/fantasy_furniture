package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import java.util.Arrays;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.BodyCreamStackGeoModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 摞体单层 Pass：按 {@link BodyCreamStackRenderState} 仅显示目标 {@code blockN} 骨骼。 */
final class BodyCreamStackLayerRenderer extends GeoBlockRenderer<BodyCreamBlockEntity> {

    private static final Set<String> LAYER_BONES =
            Set.copyOf(Arrays.asList(BodyCreamAssets.STACK_LAYER_BONES));

    BodyCreamStackLayerRenderer() {
        super(new BodyCreamStackGeoModel());
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            BodyCreamBlockEntity animatable,
            BakedGeoModel model,
            net.minecraft.client.renderer.RenderType renderType,
            MultiBufferSource bufferSource,
            com.mojang.blaze3d.vertex.VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        Map<GeoBone, Boolean> oldHidden = applyLayerBoneVisibility(model);
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
            restoreLayerBoneVisibility(oldHidden);
        }
    }

    private static Map<GeoBone, Boolean> applyLayerBoneVisibility(BakedGeoModel model) {
        String visible = BodyCreamStackRenderState.visibleBone();
        Map<GeoBone, Boolean> oldHidden = new HashMap<>();
        if (visible == null) {
            return oldHidden;
        }
        for (GeoBone bone : flattenBones(model)) {
            if (LAYER_BONES.contains(bone.getName())) {
                oldHidden.put(bone, bone.isHidden());
                bone.setHidden(!bone.getName().equals(visible));
            }
        }
        return oldHidden;
    }

    private static void restoreLayerBoneVisibility(Map<GeoBone, Boolean> oldHidden) {
        oldHidden.forEach(GeoBone::setHidden);
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
}
