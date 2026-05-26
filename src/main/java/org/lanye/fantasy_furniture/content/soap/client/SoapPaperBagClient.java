package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagMaterials;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SoapPaperBagClient {

    public static final ResourceLocation BAG_MATERIAL_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "bag_material");

    private SoapPaperBagClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                        net.minecraft.client.renderer.item.ItemProperties.register(
                                ModBlocks.SOAP_PAPER_BAG.item().get(),
                                BAG_MATERIAL_PROPERTY,
                                (stack, level, entity, seed) ->
                                        SoapPaperBagAppearance.fromStack(stack).bagMaterialId() * 0.01f));
    }

    /** 物品栏 UI 图 predicate：材质 id {@code 1}–{@link SoapPaperBagMaterials#COUNT} → {@code 0.01}–{@code 0.07}。 */
    public static float materialPropertyValue(int bagMaterialId) {
        return bagMaterialId * 0.01f;
    }
}
