package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SoapPaperBoxClient {

    public static final ResourceLocation BOX_MATERIAL_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "box_material");

    private SoapPaperBoxClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                        net.minecraft.client.renderer.item.ItemProperties.register(
                                ModBlocks.SOAP_PAPER_BOX.item().get(),
                                BOX_MATERIAL_PROPERTY,
                                (stack, level, entity, seed) ->
                                        SoapPaperBoxAppearance.fromStack(stack).materialId() * 0.01f));
    }
}
