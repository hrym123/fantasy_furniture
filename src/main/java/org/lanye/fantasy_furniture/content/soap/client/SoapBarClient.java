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
                () -> {
                    var soapBar = ModBlocks.SOAP_BAR.item().get();
                    net.minecraft.client.renderer.item.ItemProperties.register(
                            soapBar,
                            SOAP_MATERIAL_PROPERTY,
                            (stack, level, entity, seed) ->
                                    materialPropertyValue(
                                            SoapBarAppearance.fromStack(stack).materialId()));
                    // 套袋态物品栏 / Jade：用袋色 predicate 选包装袋 UI，不暴露皂色
                    net.minecraft.client.renderer.item.ItemProperties.register(
                            soapBar,
                            SoapPaperBagClient.BAG_MATERIAL_PROPERTY,
                            (stack, level, entity, seed) -> {
                                SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
                                return appearance.isBagged()
                                        ? materialPropertyValue(appearance.bagMaterialId())
                                        : 0f;
                            });
                    // 套盒态：包装盒 UI，不暴露皂色
                    net.minecraft.client.renderer.item.ItemProperties.register(
                            soapBar,
                            SoapPaperBoxClient.BOX_MATERIAL_PROPERTY,
                            (stack, level, entity, seed) -> {
                                SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
                                return appearance.isBoxed()
                                        ? materialPropertyValue(appearance.boxMaterialId())
                                        : 0f;
                            });
                });
    }

    /**
     * 物品栏 predicate：材质 id {@code 1}–{@link org.lanye.fantasy_furniture.content.soap.SoapBarMaterials#COUNT}
     * → {@code 0.01}–{@code 0.06}。须 {@code (float)(id * 0.01)}，{@code id * 0.01f} 在 id=5 时略小于 {@code 0.05f} 会误选 ui_4。
     */
    public static float materialPropertyValue(int materialId) {
        return (float) (materialId * 0.01);
    }
}
