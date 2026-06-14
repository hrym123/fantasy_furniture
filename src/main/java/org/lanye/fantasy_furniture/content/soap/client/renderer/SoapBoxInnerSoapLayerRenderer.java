package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBoxInnerSoapGeoModel;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

/** 肥皂盒内皂叠层；渲染后复位骨骼 hidden，避免与盒体 Pass 互相污染。 */
final class SoapBoxInnerSoapLayerRenderer extends ReverieGeoBlockRenderer<SoapBoxBlockEntity> {

    SoapBoxInnerSoapLayerRenderer() {
        super(new SoapBoxInnerSoapGeoModel(), GeoRenderTier.STATIC);
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            SoapBoxBlockEntity animatable,
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
        try {
            for (GeoBone bone : flattenBones(model)) {
                bone.setHidden(false);
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
            for (GeoBone bone : flattenBones(model)) {
                bone.setHidden(false);
            }
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
}
