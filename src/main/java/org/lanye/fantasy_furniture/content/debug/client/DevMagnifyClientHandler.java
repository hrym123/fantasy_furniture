package org.lanye.fantasy_furniture.content.debug.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 按住 {@link DevMagnifyKeys#MAGNIFY_HOLD} 时缩小 FOV 修正系数，等效拉近镜头（仅测试用）。
 */
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DevMagnifyClientHandler {

    /** 越小越「放大」；与原版望远镜叠加时仍受 clamp 限制。 */
    private static final float ZOOM_FACTOR = 0.22f;

    private DevMagnifyClientHandler() {}

    @SubscribeEvent
    public static void onComputeFovModifier(ComputeFovModifierEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player != event.getPlayer()) {
            return;
        }
        if (!DevMagnifyKeys.MAGNIFY_HOLD.isDown()) {
            return;
        }
        float current = event.getNewFovModifier();
        event.setNewFovModifier(Mth.clamp(current * ZOOM_FACTOR, 0.04f, 1f));
    }
}
