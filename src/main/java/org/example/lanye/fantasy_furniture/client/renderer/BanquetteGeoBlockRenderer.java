package org.example.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.example.lanye.fantasy_furniture.block.BanquetteBlock;
import org.example.lanye.fantasy_furniture.block.entity.BanquetteBlockEntity;
import org.example.lanye.fantasy_furniture.block.state.BanquetteShape;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 卡座拐角：在 {@link GeoBlockRenderer#rotateBlock} 之后对拐角 geo 追加 Y 旋转；左拼 {@code -90°}，右拼
 * {@code CORNER_YAW_LEFT + 90°}（相对左拼再 {@code +90°}）。碰撞箱在 {@link org.example.lanye.fantasy_furniture.block.BanquetteBlock}
 * 中单独旋转以对齐模型，此处不改动。
 */
@OnlyIn(Dist.CLIENT)
public final class BanquetteGeoBlockRenderer extends GeoBlockRenderer<BanquetteBlockEntity> {

    private static final float CORNER_YAW_LEFT = -90f;

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
            poseStack.mulPose(Axis.YP.rotationDegrees(CORNER_YAW_LEFT));
        } else if (shape == BanquetteShape.CORNER_RIGHT) {
            poseStack.mulPose(Axis.YP.rotationDegrees(CORNER_YAW_LEFT + 90f));
        }
    }
}
