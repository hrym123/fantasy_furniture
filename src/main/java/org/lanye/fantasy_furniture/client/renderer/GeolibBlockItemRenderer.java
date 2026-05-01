package org.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.lanye.fantasy_furniture.client.model.GeolibBlockItemModel;
import org.lanye.fantasy_furniture.geolib.GeolibBlockItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** GeckoLib 方块物品统一渲染器（调试埋点已移除）。 */
public final class GeolibBlockItemRenderer extends GeoItemRenderer<GeolibBlockItem> {
    // #region agent log
    private static long agentLastLogMs = 0L;

    private static void agentDbg(String hypothesisId, String location, String message, String dataJson) {
        // 调试埋点已关闭。
    }
    // #endregion

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
        String itemId = this.currentItemStack == null ? "" : String.valueOf(this.currentItemStack.getItem());
        boolean isSweeperDock = itemId.contains("sweeper_dock");
        // #region agent log
        if (!isReRender && this.currentItemStack != null) {
            if (itemId.contains("sweeper")) {
                long now = System.currentTimeMillis();
                if (now - agentLastLogMs >= 300L) {
                    agentLastLogMs = now;
                    ResourceLocation modelRes = animatable.assets().model();
                    ResourceLocation texRes = animatable.assets().texture();
                    ItemDisplayContext ctx = this.renderPerspective;
                    agentDbg(
                            "H5",
                            "GeolibBlockItemRenderer:actuallyRender",
                            "item_render_context",
                            "{\"item\":\""
                                    + itemId
                                    + "\",\"context\":\""
                                    + (ctx == null ? "null" : ctx.name())
                                    + "\",\"model\":\""
                                    + modelRes
                                    + "\",\"texture\":\""
                                    + texRes
                                    + "\",\"isSweeperDock\":"
                                    + isSweeperDock
                                    + "}");
                }
            }
        }
        // #endregion
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
