package org.lanye.fantasy_furniture.content.furniture.common.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.model.BanquetteBlockGeoModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BanquetteGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.client.renderer.CombinedOrnamentGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.client.renderer.PlainGlassWindowGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.SoapBarClient;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapBoxGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapPaperBagGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapRackGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.common.client.renderer.FurnitureSeatRenderer;
import org.lanye.fantasy_furniture.content.sweeper.client.renderer.SweeperRobotRenderer;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;
import org.lanye.fantasy_furniture.content.sweeper.menu.ModMenuTypes;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate6Registration;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.geolib.client.GeolibAnimatedBlockRenderers;

@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    static {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.LOTTERY_MACHINE,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "lottery_machine"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.GREEN_SOFA,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "green_sofa"));
        BedPlate6Registration.registerClientRenderer();
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.COMBINED_ORNAMENT, ctx -> new CombinedOrnamentGeoBlockRenderer());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BANQUETTE, ctx -> new BanquetteGeoBlockRenderer(new BanquetteBlockGeoModel()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SWEEPER_DOCK,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID, "sweeper_dock", SweeperDockBlockEntity::getTextureLocation));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.PLAIN_GLASS_WINDOW, ctx -> new PlainGlassWindowGeoBlockRenderer());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_BAR, SoapBarClient.blockRendererProvider());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_BOX, ctx -> new SoapBoxGeoBlockRenderer());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_RACK, ctx -> new SoapRackGeoBlockRenderer());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_PAPER_BAG, ctx -> new SoapPaperBagGeoBlockRenderer());
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BODY_WASH,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "body_wash"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SHAMPOO,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "shampoo"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BODY_CREAM,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "body_cream"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_PAPER_BOX,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "soap_paper_box"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_MOLD,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(FantasyFurniture.MODID, "soap_mold"));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.GEOLIB_ALIGNMENT_PROBE,
                GeolibAnimatedBlockRenderers.defaultGeoRendererProvider(
                        FantasyFurniture.MODID, "geolib_alignment_probe"));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        AnimatedBlockClientRegistration.registerAllRenderers(event);
        event.registerEntityRenderer(ModEntities.FURNITURE_SEAT.get(), FurnitureSeatRenderer::new);
        event.registerEntityRenderer(ModEntities.SWEEPER_ROBOT.get(), SweeperRobotRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.SWEEPER_ROBOT.get(), ContainerScreen::new));
    }
}
