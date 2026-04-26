package org.lanye.fantasy_furniture.compat.jade;

import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.entity.SweeperRobotEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum SweeperRobotJadeProvider implements IEntityComponentProvider {
    INSTANCE;

    @SuppressWarnings("removal")
    private static final ResourceLocation UID =
            new ResourceLocation(FantasyFurniture.MODID, "sweeper_robot");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Entity entity = accessor.getEntity();
        if (!(entity instanceof SweeperRobotEntity robot)) {
            return;
        }
        int hp = Mth.ceil(robot.getHealth());
        int max = Mth.ceil(robot.getMaxHealth());
        tooltip.add(Component.translatable("jade.fantasy_furniture.sweeper_robot.health", hp, max));
    }
}
