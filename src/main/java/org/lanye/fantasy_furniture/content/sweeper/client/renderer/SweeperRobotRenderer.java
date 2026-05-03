package org.lanye.fantasy_furniture.content.sweeper.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.lanye.fantasy_furniture.content.sweeper.client.model.SweeperRobotGeoModel;
import org.lanye.fantasy_furniture.content.sweeper.entity.SweeperRobotEntity;
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
                double liftY = SweeperRobotEntity.wallClimbRenderVerticalShift(entity.getType());
                if (liftY > 1.0e-4D) {
                    poseStack.translate(0.0D, liftY, 0.0D);
                }
                translateWallClimbModelBeforeTilt(
                        poseStack, wall, yaw, SweeperRobotEntity.wallClimbRenderNormalShift(entity.getType()));
                Quaternionf qTilt = SweeperRobotEntity.wallClimbTiltQuaternion(wall, yaw);
                poseStack.mulPose(qTilt);
            }
        }
    }

    /**
     * 在 GeckoLib 已施加 {@code YP(180°-yaw)} 之后、贴墙俯仰之前，把世界系「朝墙平移」换算为 PoseStack 局部平移，
     * 使模型与 {@link SweeperRobotEntity#makeBoundingBox()} 的薄向 {@code a} 对齐（不改动服务端碰撞盒）。
     * 竖直方向由调用方先 {@code translate(0, wallClimbRenderVerticalShift, 0)}。
     */
    private static void translateWallClimbModelBeforeTilt(
            PoseStack poseStack, Direction wall, float yawDegrees, double normalShift) {
        if (normalShift <= 1.0e-4D) {
            return;
        }
        Vec3 world = new Vec3(-wall.getStepX(), 0.0D, -wall.getStepZ()).scale(normalShift);
        float thetaRad = (180.0F - yawDegrees) * Mth.DEG_TO_RAD;
        Vec3 local = world.yRot(-thetaRad);
        poseStack.translate(local.x, local.y, local.z);
    }
}
