package org.lanye.fantasy_furniture.content.sweeper.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.sweeper.entity.SweeperRobotEntity;
import software.bernie.geckolib.model.GeoModel;

/** 扫地机器人 Geo 模型资源定位。 */
public final class SweeperRobotGeoModel extends GeoModel<SweeperRobotEntity> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "geo/entity/sweeper/sweeper_robot.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "textures/entity/sweeper_robot.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/entity/sweeper/sweeper_robot.animation.json");

    @Override
    public ResourceLocation getModelResource(SweeperRobotEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SweeperRobotEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SweeperRobotEntity animatable) {
        return ANIMATION;
    }
}
