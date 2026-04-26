package org.lanye.fantasy_furniture.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import org.lanye.fantasy_furniture.entity.SweeperRobotEntity;
import org.lanye.fantasy_furniture.registry.ModMenuTypes;

/** 与原版小箱子相同的 9×3 布局，绑定 {@link SweeperRobotEntity} 背包格。 */
public class SweeperRobotMenu extends ChestMenu {

    private final SweeperRobotEntity robot;

    public SweeperRobotMenu(int containerId, Inventory playerInventory, SweeperRobotEntity robot) {
        super(ModMenuTypes.SWEEPER_ROBOT.get(), containerId, playerInventory, robot, 3);
        this.robot = robot;
    }

    public static SweeperRobotMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        Entity entity = playerInventory.player.level().getEntity(entityId);
        if (entity instanceof SweeperRobotEntity robot) {
            return new SweeperRobotMenu(containerId, playerInventory, robot);
        }
        throw new IllegalStateException("Sweeper robot entity not found: " + entityId);
    }

    @Override
    public boolean stillValid(Player player) {
        return robot.stillValid(player);
    }
}
