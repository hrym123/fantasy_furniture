package org.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.lanye.fantasy_furniture.client.model.SweeperRobotGeoModel;
import org.lanye.fantasy_furniture.entity.SweeperRobotEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 扫地机器人实体渲染器。 */
@OnlyIn(Dist.CLIENT)
public final class SweeperRobotRenderer extends GeoEntityRenderer<SweeperRobotEntity> {
    // #region agent log
    private static final Path AGENT_DEBUG_LOG =
            Path.of("d:/warehouse/Lanye-mod/development/core/fantasy_furniture/debug-5a5497.log");
    private static long agentLastLogMs = 0L;

    private static void agentDbg(String hypothesisId, String location, String message, String dataJson) {
        try {
            String line = "{\"sessionId\":\"5a5497\",\"runId\":\"run_render_1\",\"hypothesisId\":\""
                    + hypothesisId
                    + "\",\"location\":\""
                    + location
                    + "\",\"message\":\""
                    + message
                    + "\",\"data\":"
                    + dataJson
                    + ",\"timestamp\":"
                    + System.currentTimeMillis()
                    + "}\n";
            Files.writeString(AGENT_DEBUG_LOG, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
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
                // 世界系贴墙旋转轴：n×(0,1,0)，n 为墙面法线（与实体侧速度投影所用一致）
                float ax = -wall.getStepZ();
                float az = wall.getStepX();
                float yR = yaw * Mth.DEG_TO_RAD;
                float lx = ax * Mth.cos(yR) + az * Mth.sin(yR);
                float lz = -ax * Mth.sin(yR) + az * Mth.cos(yR);
                float len = Mth.sqrt(lx * lx + lz * lz);
                if (len > 1.0e-4f) {
                    lx /= len;
                    lz /= len;
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
                    poseStack.mulPose(
                            new Quaternionf().rotateAxis(Mth.PI / 2.0f, lx, 0.0f, lz));
                }
            }
        }
    }
}
