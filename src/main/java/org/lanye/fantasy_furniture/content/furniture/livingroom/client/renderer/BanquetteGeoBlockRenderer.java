package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.common.client.config.ClientRenderTuning;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.common.state.BanquetteShape;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 卡座拐角：在 {@link GeoBlockRenderer#rotateBlock} 之后对拐角 geo 追加 Y 旋转；角度见
 * {@link org.lanye.fantasy_furniture.content.furniture.common.client.config.ClientRenderTuning.Banquette}。碰撞箱在
 * {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock} 中单独旋转以对齐模型，此处不改动。
 */
@OnlyIn(Dist.CLIENT)
public final class BanquetteGeoBlockRenderer extends GeoBlockRenderer<BanquetteBlockEntity> {

    public BanquetteGeoBlockRenderer(GeoModel<BanquetteBlockEntity> model) {
        super(model);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        super.rotateBlock(facing, poseStack);
        if (animatable == null) {
            return;
        }
        BanquetteShape shape = animatable.getBlockState().getValue(BanquetteBlock.SHAPE);
        if (shape == BanquetteShape.CORNER_LEFT) {
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(ClientRenderTuning.Banquette.CORNER_YAW_LEFT_DEG));
        } else if (shape == BanquetteShape.CORNER_RIGHT) {
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                            ClientRenderTuning.Banquette.CORNER_YAW_LEFT_DEG
                                    + ClientRenderTuning.Banquette.CORNER_YAW_RIGHT_OFFSET_DEG));
        }
    }
}
