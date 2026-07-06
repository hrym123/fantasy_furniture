package org.lanye.fantasy_furniture.content.furniture.common.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.model.BanquetteBlockGeoModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BanquetteGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.client.renderer.PlainGlassWindowGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.SoapBarClient;
import org.lanye.fantasy_furniture.content.soap.client.renderer.BodyCreamGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.BodyWashGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapBoxGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapMoldGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapPaperBagGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapPaperBoxGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.DisplayCabinetGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapRackGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.ShampooGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.common.client.renderer.FurnitureSeatRenderer;
import org.lanye.fantasy_furniture.content.sweeper.client.renderer.SweeperRobotRenderer;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;
import org.lanye.fantasy_furniture.content.sweeper.menu.ModMenuTypes;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate6Registration;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.geolib.client.GeolibAnimatedBlockRenderers;
import org.lanye.reverie_core.util.ReveriePerfRender;

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
                ModBlocks.BANQUETTE,
                ReveriePerfRender.wrapBer(ctx -> new BanquetteGeoBlockRenderer(new BanquetteBlockGeoModel())));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SWEEPER_DOCK,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID, "sweeper_dock", SweeperDockBlockEntity::getTextureLocation));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.PLAIN_GLASS_WINDOW,
                ReveriePerfRender.wrapBer(ctx -> new PlainGlassWindowGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_BAR, ReveriePerfRender.wrapBer(SoapBarClient.blockRendererProvider()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_BOX, ReveriePerfRender.wrapBer(ctx -> new SoapBoxGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_RACK, ReveriePerfRender.wrapBer(ctx -> new SoapRackGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_PAPER_BAG, ReveriePerfRender.wrapBer(ctx -> new SoapPaperBagGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BODY_WASH, ReveriePerfRender.wrapBer(ctx -> new BodyWashGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SHAMPOO, ReveriePerfRender.wrapBer(ctx -> new ShampooGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BODY_CREAM, ReveriePerfRender.wrapBer(ctx -> new BodyCreamGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_PAPER_BOX, ReveriePerfRender.wrapBer(ctx -> new SoapPaperBoxGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SOAP_MOLD, ReveriePerfRender.wrapBer(SoapMoldGeoBlockRenderer::new));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.DISPLAY_CABINET, ReveriePerfRender.wrapBer(ctx -> new DisplayCabinetGeoBlockRenderer()));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "block/internal/soap_packaging_particle_stitch"));
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
