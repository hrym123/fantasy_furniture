package org.lanye.fantasy_furniture.content.debug;

import org.lanye.reverie_core.debug.FantasyDebugVariantHandlers;
import org.lanye.reverie_core.debug.ReverieDevelopmentMode;

/** 向 reverie_core 注册幻想调试棒变体循环与开发模式开关。 */
public final class FantasyFurnitureDebugRegistration {

    private FantasyFurnitureDebugRegistration() {}

    public static void register() {
        ReverieDevelopmentMode.setEnabledSupplier(DevelopmentMode::enabled);
        FantasyDebugVariantHandlers.register(
                new FantasyDebugVariantHandlerAdapter(ModVariantCycler::cycleBlock, ModVariantCycler::cycleItemStack));
    }
}
