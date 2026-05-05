package org.lanye.fantasy_furniture.content.furniture.common.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.PlainWindowBlocks;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainWindowMaterialItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.model.BanquetteBlockGeoModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BanquetteGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.client.renderer.CombinedOrnamentGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.common.client.renderer.FurnitureSeatRenderer;
import org.lanye.fantasy_furniture.content.sweeper.client.renderer.SweeperRobotRenderer;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;
import org.lanye.fantasy_furniture.content.sweeper.menu.ModMenuTypes;
import org.lanye.reverie_core.client.BlockRenderLayers;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.geolib.client.GeolibAnimatedBlockRenderers;

@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    static {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.MIXING_BOWL,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "mixing_bowl"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.JAM_POT,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "jam_pot"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.OVEN,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "oven"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.PESTLE_BOWL,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "pestle_bowl"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.HALF_HALF_POT,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "half_half_pot"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.LOTTERY_MACHINE,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "lottery_machine"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.GREEN_SOFA,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "green_sofa"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.KITCHEN_COUNTER_CABINET,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(
                        FantasyFurniture.MODID, "kitchen_counter_cabinet"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.KITCHEN_COUNTER,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "kitchen_counter"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.COMBINED_ORNAMENT, ctx -> new CombinedOrnamentGeoBlockRenderer());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BANQUETTE, ctx -> new BanquetteGeoBlockRenderer(new BanquetteBlockGeoModel()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SWEEPER_DOCK,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID, "sweeper_dock", SweeperDockBlockEntity::getTextureLocation));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        AnimatedBlockClientRegistration.registerAllRenderers(event);
        event.registerEntityRenderer(ModEntities.FURNITURE_SEAT.get(), FurnitureSeatRenderer::new);
        event.registerEntityRenderer(ModEntities.SWEEPER_ROBOT.get(), SweeperRobotRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                    MenuScreens.register(ModMenuTypes.SWEEPER_ROBOT.get(), ContainerScreen::new);
                    // 普通窗户：纯 JSON + BlockItem；54 种 id 共用 cutout（见 PlainWindowBlocks）。
                    Block[] plainWindows =
                            PlainWindowBlocks.blockEntries().stream()
                                    .map(e -> e.block().get())
                                    .toArray(Block[]::new);
                    BlockRenderLayers.registerCutout(plainWindows);
                    // #region agent log
                    AgentDebugLog.log(
                            "H1",
                            "ClientModEvents.onClientSetup",
                            "cutout plain windows",
                            "{\"plainWindowBlockCount\":" + plainWindows.length + "}");
                    // #endregion
                });
    }

    @SubscribeEvent
    public static void registerPlainWindowBlockColors(RegisterColorHandlersEvent.Block event) {
        Block[] plainWindows =
                PlainWindowBlocks.blockEntries().stream()
                        .map(e -> e.block().get())
                        .toArray(Block[]::new);
        // #region agent log
        AgentDebugLog.log(
                "H2",
                "ClientModEvents.registerPlainWindowBlockColors",
                "register block tint handlers",
                "{\"blockCount\":" + plainWindows.length + "}");
        // #endregion
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex != 0) {
                        return -1;
                    }
                    if (state.getBlock() instanceof PlainWindowBlock block) {
                        return PlainWindowColors.tintRgb(block.material());
                    }
                    return -1;
                },
                plainWindows);
    }

    @SubscribeEvent
    public static void registerPlainWindowItemColors(RegisterColorHandlersEvent.Item event) {
        Item[] items =
                PlainWindowBlocks.materialItemEntries().stream()
                        .map(e -> e.item().get())
                        .toArray(Item[]::new);
        // #region agent log
        AgentDebugLog.log(
                "H2",
                "ClientModEvents.registerPlainWindowItemColors",
                "register item tint handlers",
                "{\"itemCount\":" + items.length + "}");
        // #endregion
        event.register(
                (stack, tintIndex) -> {
                    if (tintIndex != 0) {
                        return -1;
                    }
                    if (stack.getItem() instanceof PlainWindowMaterialItem item) {
                        return PlainWindowColors.tintRgb(item.material());
                    }
                    return -1;
                },
                items);
    }
}
