package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.DisplayCabinetBottleRenderState;
import org.lanye.fantasy_furniture.content.soap.client.model.DisplayCabinetGeoModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 按 {@link DisplayCabinetBottleRenderState} 控制瓶罐骨骼可见性。 */
final class DisplayCabinetLayerRenderer extends GeoBlockRenderer<DisplayCabinetBlockEntity> {

    DisplayCabinetLayerRenderer(GeoModel<DisplayCabinetBlockEntity> model) {
        super(model);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            DisplayCabinetBlockEntity animatable,
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
        Map<GeoBone, Boolean> oldHidden = applyManagedBoneVisibility(model);
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
            restoreBoneVisibility(oldHidden);
        }
    }

    private static Map<GeoBone, Boolean> applyManagedBoneVisibility(BakedGeoModel model) {
        DisplayCabinetBottleRenderState.Pass pass = DisplayCabinetBottleRenderState.pass();
        if (pass == DisplayCabinetBottleRenderState.Pass.DEFAULT) {
            return Map.of();
        }
        Set<String> managed = DisplayCabinetBottleRenderState.managedBones();
        if (pass == DisplayCabinetBottleRenderState.Pass.CABINET_ONLY && managed.isEmpty()) {
            return Map.of();
        }
        Set<String> visible =
                pass == DisplayCabinetBottleRenderState.Pass.BOTTLE_ONLY
                        ? DisplayCabinetBottleRenderState.visibleBones()
                        : Set.of();
        Map<GeoBone, Boolean> oldHidden = new HashMap<>();
        for (GeoBone bone : flattenBones(model)) {
            String name = bone.getName();
            boolean hide =
                    switch (pass) {
                        case CABINET_ONLY -> managed.contains(name);
                        case BOTTLE_ONLY -> !visible.contains(name);
                        default -> false;
                    };
            if (!hide) {
                continue;
            }
            oldHidden.put(bone, bone.isHidden());
            bone.setHidden(true);
        }
        return oldHidden;
    }

    private static void restoreBoneVisibility(Map<GeoBone, Boolean> oldHidden) {
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
