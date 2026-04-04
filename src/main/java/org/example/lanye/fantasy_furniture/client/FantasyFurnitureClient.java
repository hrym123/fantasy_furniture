package org.example.lanye.fantasy_furniture.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.ModBlocks;

/**
 * 客户端：渲染层等与纯服务端无关的注册。
 */
@Mod.EventBusSubscriber(modid = Fantasy_furniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FantasyFurnitureClient {

    private FantasyFurnitureClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.DECORATIVE_SCREEN_BLOCK.get(), RenderType.cutout()));
    }
}
