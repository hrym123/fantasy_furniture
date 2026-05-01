package org.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lanye.fantasy_furniture.client.model.SweeperRobotGeoModel;
import org.lanye.fantasy_furniture.entity.SweeperRobotEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 扫地机器人实体渲染器。 */
@OnlyIn(Dist.CLIENT)
public final class SweeperRobotRenderer extends GeoEntityRenderer<SweeperRobotEntity> {
    public SweeperRobotRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SweeperRobotGeoModel());
        this.shadowRadius = 0.35f;
    }

    @Override
    protected void applyRotations(
            SweeperRobotEntity entity,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick) {
        // GeckoLib 默认传入的是 yBodyRot 插值；LivingEntity 身体朝向滞后于 yRot，模型会不跟转。
        float yaw = entity.getVisualYaw(partialTick);
        super.applyRotations(entity, poseStack, ageInTicks, yaw, partialTick);
        if (entity.isWallClimbing()) {
            Direction wall = entity.getWallClimbFacing();
            if (wall != null && wall.getAxis().isHorizontal()) {
                // 以墙面法线为基准先得到世界切向轴，再按当前 yaw 逆变换到本地轴。
                float ax = -wall.getStepZ();
                float az = wall.getStepX();
                float yR = yaw * Mth.DEG_TO_RAD;
                float lx = ax * Mth.cos(yR) + az * Mth.sin(yR);
                float lz = -ax * Mth.sin(yR) + az * Mth.cos(yR);
                float len = Mth.sqrt(lx * lx + lz * lz);
                if (len > 1.0e-4f) {
                    lx /= len;
                    lz /= len;
                    Quaternionf qPlus = new Quaternionf().rotateAxis(Mth.PI / 2.0f, lx, 0.0f, lz);
                    Quaternionf qMinus = new Quaternionf().rotateAxis(-Mth.PI / 2.0f, lx, 0.0f, lz);
                    float cosY = Mth.cos(yR);
                    float sinY = Mth.sin(yR);
                    Vector3f downLocalPlus = new Vector3f(0.0f, -1.0f, 0.0f).rotate(qPlus);
                    float downPlusWorldX = downLocalPlus.x * cosY - downLocalPlus.z * sinY;
                    float downPlusWorldZ = downLocalPlus.x * sinY + downLocalPlus.z * cosY;
                    float downDotPlus = downPlusWorldX * wall.getStepX() + downPlusWorldZ * wall.getStepZ();
                    Vector3f downLocalMinus = new Vector3f(0.0f, -1.0f, 0.0f).rotate(qMinus);
                    float downMinusWorldX = downLocalMinus.x * cosY - downLocalMinus.z * sinY;
                    float downMinusWorldZ = downLocalMinus.x * sinY + downLocalMinus.z * cosY;
                    float downDotMinus = downMinusWorldX * wall.getStepX() + downMinusWorldZ * wall.getStepZ();
                    boolean usePlus = downDotPlus >= downDotMinus;
                    Quaternionf qTilt = usePlus ? qPlus : qMinus;
                    poseStack.mulPose(qTilt);
                }
            }
        }
    }
}
