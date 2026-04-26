package org.lanye.fantasy_furniture.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    }
}
