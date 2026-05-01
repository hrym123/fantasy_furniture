package org.lanye.fantasy_furniture.content.furniture.common.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lanye.fantasy_furniture.content.furniture.common.client.model.GeolibBlockItemModel;
import org.lanye.fantasy_furniture.core.geolib.GeolibBlockItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** GeckoLib 方块物品统一渲染器。 */
public final class GeolibBlockItemRenderer extends GeoItemRenderer<GeolibBlockItem> {
    public GeolibBlockItemRenderer() {
        super(new GeolibBlockItemModel());
    }

    @Override
    public void actuallyRender(
            PoseStack poseStack,
            GeolibBlockItem animatable,
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
    }
}
