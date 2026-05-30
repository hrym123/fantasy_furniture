package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.renderer.SoapBarGeoBlockRenderer;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SoapBarClient {

    public static final ResourceLocation SOAP_MATERIAL_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "soap_material");

    private SoapBarClient() {}

    public static BlockEntityRendererProvider<SoapBarBlockEntity> blockRendererProvider() {
        return ctx -> new SoapBarGeoBlockRenderer();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () ->
                        net.minecraft.client.renderer.item.ItemProperties.register(
                                ModBlocks.SOAP_BAR.item().get(),
                                SOAP_MATERIAL_PROPERTY,
                                (stack, level, entity, seed) ->
                                        materialPropertyValue(
                                                SoapBarAppearance.fromStack(stack).materialId())));
    }

    /**
     * 物品栏 predicate：材质 id {@code 1}–{@link org.lanye.fantasy_furniture.content.soap.SoapBarMaterials#COUNT}
     * → {@code 0.01}–{@code 0.06}。须 {@code (float)(id * 0.01)}，{@code id * 0.01f} 在 id=5 时略小于 {@code 0.05f} 会误选 ui_4。
     */
    public static float materialPropertyValue(int materialId) {
        return (float) (materialId * 0.01);
    }
}
