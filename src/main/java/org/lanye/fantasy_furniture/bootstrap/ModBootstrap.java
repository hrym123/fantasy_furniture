package org.lanye.fantasy_furniture.bootstrap;

import net.minecraftforge.eventbus.api.IEventBus;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;
import org.lanye.fantasy_furniture.content.sweeper.menu.ModMenuTypes;
import org.lanye.fantasy_furniture.content.seat.ModSeatConfigs;
import org.lanye.fantasy_furniture.bootstrap.item.ModCreativeTabs;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.content.debug.FantasyFurnitureDebugRegistration;
import org.lanye.fantasy_furniture.content.tool.FantasyFurniturePaintBrushRegistration;
import org.lanye.fantasy_furniture.content.tool.FantasyFurnitureSpoolRegistration;
import org.lanye.reverie_core.content.fantasy_core.workbench.FantasyWorkbenchCatalogRegistry;

/** 统一注册编排入口，避免主类直接依赖大量注册器。 */
public final class ModBootstrap {

    private ModBootstrap() {}

    public static void register(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }

    public static void registerCommonWork() {
        FantasyFurnitureDebugRegistration.register();
        FantasyFurniturePaintBrushRegistration.register();
        FantasyFurnitureSpoolRegistration.register();
        FantasyWorkbenchCatalogRegistry.registerContentMod(FantasyFurniture.MODID);
        ModSeatConfigs.register();
    }
}

