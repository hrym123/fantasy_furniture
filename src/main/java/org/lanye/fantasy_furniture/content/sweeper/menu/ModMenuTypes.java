package org.lanye.fantasy_furniture.content.sweeper.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.sweeper.menu.SweeperRobotMenu;

public final class ModMenuTypes {

    private ModMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FantasyFurniture.MODID);

    public static final RegistryObject<MenuType<SweeperRobotMenu>> SWEEPER_ROBOT =
            MENU_TYPES.register("sweeper_robot", () -> IForgeMenuType.create(SweeperRobotMenu::fromNetwork));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
