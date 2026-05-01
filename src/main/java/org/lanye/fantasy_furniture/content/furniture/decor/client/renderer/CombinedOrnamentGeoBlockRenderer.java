package org.lanye.fantasy_furniture.content.furniture.decor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.client.model.CombinedOrnamentBaseGeoModel;
import org.lanye.fantasy_furniture.content.furniture.decor.client.model.CombinedOrnamentFigurineGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 子模型差分：同帧依次渲染底座 Geo 与玩偶 Geo（各 {@link GeoBlockRenderer} 一套资源）。
 */
@OnlyIn(Dist.CLIENT)
public final class CombinedOrnamentGeoBlockRenderer implements BlockEntityRenderer<CombinedOrnamentBlockEntity> {

    private final GeoBlockRenderer<CombinedOrnamentBlockEntity> baseRenderer =
            new GeoBlockRenderer<>(new CombinedOrnamentBaseGeoModel());
    private final GeoBlockRenderer<CombinedOrnamentBlockEntity> figurineRenderer =
            new GeoBlockRenderer<>(new CombinedOrnamentFigurineGeoModel());

    @Override
    public void render(
            CombinedOrnamentBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        this.baseRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        this.figurineRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
