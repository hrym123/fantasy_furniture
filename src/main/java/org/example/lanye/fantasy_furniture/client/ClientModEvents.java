package org.example.lanye.fantasy_furniture.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.ModBlocks;
import org.example.lanye.fantasy_furniture.client.renderer.MixingBowlBlockRenderer;
import org.example.lanye.fantasy_furniture.geolib.client.AnimatedBlockClientRegistration;

@Mod.EventBusSubscriber(modid = Fantasy_furniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    static {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                ModBlocks.MIXING_BOWL, ctx -> new MixingBowlBlockRenderer());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        AnimatedBlockClientRegistration.registerAllRenderers(event);
    }
}
