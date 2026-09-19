package org.lanye.fantasy_furniture.content.furniture.common.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.particle.ModParticles;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidMaterials;
import org.lanye.fantasy_furniture.content.soap.client.SoapDissolveParticle;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.CabinetGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.bar.blockentity.BarCounterBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.bar.blockentity.CornerBarBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.client.model.ComputerGeoModel;
import org.lanye.fantasy_furniture.content.furniture.decor.client.model.DrinkwareGeoModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.model.BanquetteBlockGeoModel;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BanquetteGeoBlockRenderer;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import org.lanye.fantasy_furniture.bootstrap.block.StyledWindowSeriesRegistration;
import org.lanye.fantasy_furniture.content.furniture.decor.client.renderer.StyledWindow0GeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.StyledStairsBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesId;
import org.lanye.fantasy_furniture.content.soap.client.SoapBarClient;
import org.lanye.fantasy_furniture.content.soap.client.renderer.BodyCreamGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.BodyWashGeoBlockRenderer;
import org.lanye.fantasy_furniture.content.soap.client.renderer.BubbleHeadLayer;
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
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate1Registration;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlateBasicRegistration;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate2Registration;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate3Registration;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate6Registration;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;
import org.lanye.reverie_core.geolib.client.GeolibAnimatedBlockRenderers;
import org.lanye.reverie_core.util.ReveriePerfRender;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;

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
        BedPlate1Registration.registerClientRenderers();
        BedPlateBasicRegistration.registerClientRenderers();
        BedPlate2Registration.registerClientRenderer();
        BedPlate3Registration.registerClientRenderers();
        BedPlate6Registration.registerClientRenderer();
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BANQUETTE,
                ReveriePerfRender.wrapBer(ctx -> new BanquetteGeoBlockRenderer(new BanquetteBlockGeoModel())));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.SWEEPER_DOCK,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID, "sweeper_dock", SweeperDockBlockEntity::getTextureLocation));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.STYLED_WINDOW_0,
                ReveriePerfRender.wrapBer(ctx -> new StyledWindow0GeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.STYLED_STAIRS,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID,
                        "styled_stairs",
                        StyledStairsBlockEntity::getTextureLocation));
        for (StyledWindowSeriesId seriesId : StyledWindowSeriesId.values()) {
            AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                    StyledWindowSeriesRegistration.entry(seriesId),
                    ReveriePerfRender.wrapBer(ctx -> new StyledWindowSeriesGeoBlockRenderer()));
        }
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
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.COMPUTER_1,
                ReveriePerfRender.wrapBer(
                        ctx -> new ReverieGeoBlockRenderer<>(new ComputerGeoModel(), GeoRenderTier.STATIC)));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.COMPUTER_2,
                ReveriePerfRender.wrapBer(
                        ctx -> new ReverieGeoBlockRenderer<>(new ComputerGeoModel(), GeoRenderTier.STATIC)));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.CABINET_1,
                ReveriePerfRender.wrapBer(ctx -> new CabinetGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.CABINET_2,
                ReveriePerfRender.wrapBer(ctx -> new CabinetGeoBlockRenderer()));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.DRINKWARE,
                ReveriePerfRender.wrapBer(
                        ctx -> new ReverieGeoBlockRenderer<>(new DrinkwareGeoModel(), GeoRenderTier.STATIC)));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.BAR_COUNTER,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID, "bar_counter", BarCounterBlockEntity::getTextureLocation));
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.CORNER_BAR,
                GeolibAnimatedBlockRenderers.variableTextureGeoRendererProvider(
                        FantasyFurniture.MODID, "corner_bar", CornerBarBlockEntity::getTextureLocation));
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        for (int id = 1; id <= SoapFlatLiquidMaterials.COUNT; id++) {
            event.registerSpriteSet(
                    ModParticles.soapDissolveObject(id).get(), SoapDissolveParticle.Provider::new);
        }
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
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new BubbleHeadLayer<>(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.SWEEPER_ROBOT.get(), ContainerScreen::new));
    }
}
