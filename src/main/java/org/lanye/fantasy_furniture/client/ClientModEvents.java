package org.lanye.fantasy_furniture.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import org.lanye.fantasy_furniture.client.model.BanquetteBlockGeoModel;
import org.lanye.fantasy_furniture.client.renderer.BanquetteGeoBlockRenderer;
import org.lanye.fantasy_furniture.client.renderer.CombinedOrnamentGeoBlockRenderer;
import org.lanye.fantasy_furniture.client.renderer.FurnitureSeatRenderer;
import org.lanye.fantasy_furniture.client.renderer.SweeperRobotRenderer;
import org.lanye.fantasy_furniture.registry.ModEntities;
import org.lanye.fantasy_furniture.registry.ModMenuTypes;
import org.lanye.fantasy_furniture.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.fantasy_furniture.geolib.client.GeolibAnimatedBlockRenderers;

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
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "sweeper_dock"));
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
                () -> MenuScreens.register(ModMenuTypes.SWEEPER_ROBOT.get(), ContainerScreen::new));
    }
}
