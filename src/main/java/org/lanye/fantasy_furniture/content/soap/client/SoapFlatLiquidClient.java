package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidAppearance;
import org.lanye.fantasy_furniture.content.soap.item.SoapFlatLiquidItem;

@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SoapFlatLiquidClient {

    public static final ResourceLocation LIQUID_MATERIAL_PREDICATE =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "liquid_material");

    private SoapFlatLiquidClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                    registerLiquidItem(ModItems.SHAMPOO_LIQUID.get());
                    registerLiquidItem(ModItems.BODY_WASH_LIQUID.get());
                });
    }

    private static void registerLiquidItem(Item item) {
        if (!(item instanceof SoapFlatLiquidItem liquid)) {
            return;
        }
        String stem = liquid.textureStem();
        ItemProperties.register(
                item,
                LIQUID_MATERIAL_PREDICATE,
                (stack, level, entity, seed) ->
                        SoapFlatLiquidAppearance.fromStack(stack, stem).materialId() * 0.01f);
    }
}
