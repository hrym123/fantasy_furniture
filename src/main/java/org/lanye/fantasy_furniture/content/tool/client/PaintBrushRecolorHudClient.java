package org.lanye.fantasy_furniture.content.tool.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;

@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PaintBrushRecolorHudClient {

    private static final ResourceLocation BRUSH_OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "paint_brush_recolor");
    private static final ResourceLocation SPOOL_OVERLAY_ID =
            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "fantasy_spool_recolor");

    private PaintBrushRecolorHudClient() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                VanillaGuiOverlay.FOOD_LEVEL.id(),
                BRUSH_OVERLAY_ID.getPath(),
                PaintBrushRecolorHudOverlay.instance());
        event.registerAbove(
                VanillaGuiOverlay.FOOD_LEVEL.id(),
                SPOOL_OVERLAY_ID.getPath(),
                FantasySpoolRecolorHudOverlay.instance());
    }
}
