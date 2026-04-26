package org.lanye.fantasy_furniture.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;

@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DecorativeHelmetRenderEvents {

    private static final Set<PlayerRenderer> DECORATIVE_HELMET_LAYER_REGISTERED =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private DecorativeHelmetRenderEvents() {}

    @SubscribeEvent
    public static void onAddPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer playerRenderer = event.getSkin(skin);
            if (playerRenderer != null && DECORATIVE_HELMET_LAYER_REGISTERED.add(playerRenderer)) {
                playerRenderer.addLayer(new DecorativeHelmetPlayerLayer(playerRenderer));
            }
        }
    }
}
