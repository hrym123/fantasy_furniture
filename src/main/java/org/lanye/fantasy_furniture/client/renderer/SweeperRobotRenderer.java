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
    // #region agent log
    private static long agentLastLogMs = 0L;
    private static long agentLastWallGeomLogMs = 0L;
    private static long agentLastWallQualityLogMs = 0L;

    private static void agentDbg(String hypothesisId, String location, String message, String dataJson) {
        // 调试埋点已关闭。
    }
    // #endregion

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
        // #region agent log
        long now = System.currentTimeMillis();
        if (now - agentLastLogMs >= 1000L) {
            agentLastLogMs = now;
            agentDbg(
                    "H3",
                    "SweeperRobotRenderer:applyRotations",
                    "entity_rotation_state",
                    "{\"entityId\":"
                            + entity.getId()
                            + ",\"yaw\":"
                            + yaw
                            + ",\"bodyYawInput\":"
                            + rotationYaw
                            + ",\"isWallClimbing\":"
                            + entity.isWallClimbing()
                            + ",\"wallFacing\":\""
                            + entity.getWallClimbFacing()
                            + "\"}");
        }
        // #endregion
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
                    float worldLen = Mth.sqrt(ax * ax + az * az);
                    float nx = wall.getStepX();
                    float nz = wall.getStepZ();
                    // #region agent log
                    if (now - agentLastWallGeomLogMs >= 250L) {
                        agentLastWallGeomLogMs = now;
                        agentDbg(
                                "H27",
                                "SweeperRobotRenderer:applyRotations",
                                "wall_rotation_geometry",
                                "{\"entityId\":"
                                        + entity.getId()
                                        + ",\"wall\":\""
                                        + wall
                                        + "\",\"yaw\":"
                                        + yaw
                                        + ",\"worldAxisX\":"
                                        + ax
                                        + ",\"worldAxisZ\":"
                                        + az
                                        + ",\"worldAxisLen\":"
                                        + worldLen
                                        + ",\"localAxisX\":"
                                        + lx
                                        + ",\"localAxisZ\":"
                                        + lz
                                        + ",\"wallNormalX\":"
                                        + nx
                                        + ",\"wallNormalZ\":"
                                        + nz
                                        + "}");
                    }
                    // #endregion
                    // #region agent log
                    if (now - agentLastLogMs >= 250L) {
                        agentLastLogMs = now;
                        agentDbg(
                                "H4",
                                "SweeperRobotRenderer:applyRotations",
                                "wall_rotation_applied",
                                "{\"entityId\":"
                                        + entity.getId()
                                        + ",\"wall\":\""
                                        + wall
                                        + "\",\"axisX\":"
                                        + lx
                                        + ",\"axisZ\":"
                                        + lz
                                        + "}");
                    }
                    // #endregion
                    Quaternionf qPlus = new Quaternionf().rotateAxis(Mth.PI / 2.0f, lx, 0.0f, lz);
                    Quaternionf qMinus = new Quaternionf().rotateAxis(-Mth.PI / 2.0f, lx, 0.0f, lz);
                    float cosY = Mth.cos(yR);
                    float sinY = Mth.sin(yR);
                    Vector3f downLocalPlus = new Vector3f(0.0f, -1.0f, 0.0f).rotate(qPlus);
                    float downPlusWorldX = downLocalPlus.x * cosY - downLocalPlus.z * sinY;
                    float downPlusWorldY = downLocalPlus.y;
                    float downPlusWorldZ = downLocalPlus.x * sinY + downLocalPlus.z * cosY;
                    float downDotPlus = downPlusWorldX * nx + downPlusWorldZ * nz;
                    Vector3f downLocalMinus = new Vector3f(0.0f, -1.0f, 0.0f).rotate(qMinus);
                    float downMinusWorldX = downLocalMinus.x * cosY - downLocalMinus.z * sinY;
                    float downMinusWorldY = downLocalMinus.y;
                    float downMinusWorldZ = downLocalMinus.x * sinY + downLocalMinus.z * cosY;
                    float downDotMinus = downMinusWorldX * nx + downMinusWorldZ * nz;
                    boolean usePlus = downDotPlus >= downDotMinus;
                    Quaternionf qTilt = usePlus ? qPlus : qMinus;
                    float downWorldX = usePlus ? downPlusWorldX : downMinusWorldX;
                    float downWorldY = usePlus ? downPlusWorldY : downMinusWorldY;
                    float downWorldZ = usePlus ? downPlusWorldZ : downMinusWorldZ;
                    float downDotWallNormal = usePlus ? downDotPlus : downDotMinus;
                    float downDotUp = downWorldY;
                    poseStack.mulPose(qTilt);
                    // #region agent log
                    if (now - agentLastWallQualityLogMs >= 250L) {
                        agentLastWallQualityLogMs = now;
                        agentDbg(
                                "H29",
                                "SweeperRobotRenderer:applyRotations",
                                "wall_rotation_quality",
                                "{\"entityId\":"
                                        + entity.getId()
                                        + ",\"wall\":\""
                                        + wall
                                        + "\",\"yaw\":"
                                        + yaw
                                        + ",\"downWorldX\":"
                                        + downWorldX
                                        + ",\"downWorldY\":"
                                        + downWorldY
                                        + ",\"downWorldZ\":"
                                        + downWorldZ
                                        + ",\"downDotWallNormal\":"
                                        + downDotWallNormal
                                        + ",\"downDotPlus\":"
                                        + downDotPlus
                                        + ",\"downDotMinus\":"
                                        + downDotMinus
                                        + ",\"chosenSign\":\""
                                        + (usePlus ? "plus" : "minus")
                                        + "\""
                                        + ",\"downDotUp\":"
                                        + downDotUp
                                        + "}");
                    }
                    // #endregion
                    // #region agent log
                    if (now - agentLastWallGeomLogMs >= 250L) {
                        agentLastWallGeomLogMs = now;
                        agentDbg(
                                "H28",
                                "SweeperRobotRenderer:applyRotations",
                                "wall_rotation_angle_applied",
                                "{\"entityId\":"
                                        + entity.getId()
                                        + ",\"angleDeg\":"
                                        + (usePlus ? "90.0" : "-90.0")
                                        + ",\"axisX\":"
                                        + lx
                                        + ",\"axisZ\":"
                                        + lz
                                        + "}");
                    }
                    // #endregion
                }
            }
        }
    }
}
