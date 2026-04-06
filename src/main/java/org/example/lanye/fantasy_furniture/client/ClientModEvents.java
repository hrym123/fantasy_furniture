package org.example.lanye.fantasy_furniture.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.ModBlocks;
import org.example.lanye.fantasy_furniture.client.model.BanquetteBlockGeoModel;
import org.example.lanye.fantasy_furniture.client.renderer.BanquetteGeoBlockRenderer;
import org.example.lanye.fantasy_furniture.geolib.client.AnimatedBlockClientRegistration;
import org.example.lanye.fantasy_furniture.geolib.client.GeolibAnimatedBlockRenderers;

@Mod.EventBusSubscriber(modid = Fantasy_furniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    static {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.MIXING_BOWL,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "mixing_bowl"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.JAM_POT,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "jam_pot"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.OVEN,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "oven"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.PESTLE_BOWL,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "pestle_bowl"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.HALF_HALF_POT,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "half_half_pot"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.LOTTERY_MACHINE,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "lottery_machine"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.GREEN_SOFA,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "green_sofa"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.KITCHEN_COUNTER_CABINET,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(
                        Fantasy_furniture.MODID, "kitchen_counter_cabinet"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.KITCHEN_COUNTER,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(Fantasy_furniture.MODID, "kitchen_counter"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BANQUETTE, ctx -> new BanquetteGeoBlockRenderer(new BanquetteBlockGeoModel()));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        AnimatedBlockClientRegistration.registerAllRenderers(event);
    }
}
