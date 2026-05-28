package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapBoxBodyGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 盒体 Geo + 可选盒内 {@code soap_box_inner_soap} 叠层（同帧两次渲染）。
 *
 * <p>肥皂盒 geo 与默认 {@link GeoBlockRenderer#rotateBlock} 已对齐，勿套用床板 +180° Y。
 * 碰撞：{@link SoapBoxBlock#getShape} 用 {@code geo_collision_box.py} 北向外接盒 +
 * {@link org.lanye.reverie_core.util.VoxelShapeRotation#rotateYFromNorthLikeGeckoBlockRenderer}。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapBoxGeoBlockRenderer implements BlockEntityRenderer<SoapBoxBlockEntity> {

    private final GeoBlockRenderer<SoapBoxBlockEntity> bodyRenderer =
            new GeoBlockRenderer<>(new SoapBoxBodyGeoModel());
    private final GeoBlockRenderer<SoapBoxBlockEntity> innerSoapRenderer =
            new SoapBoxInnerSoapLayerRenderer();

    @Override
    public void render(
            SoapBoxBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        this.bodyRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        if (blockEntity.hasContainedSoap(blockEntity.getBlockState())) {
            this.innerSoapRenderer.render(
                    blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
