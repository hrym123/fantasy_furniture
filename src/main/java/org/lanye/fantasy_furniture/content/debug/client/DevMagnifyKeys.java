package org.lanye.fantasy_furniture.content.debug.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 开发用「按住放大观察」按键（默认 C）；可在按键设置中改键或解绑。
 */
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DevMagnifyKeys {

    /** 按住时降低 FOV 等效放大画面（仅本地客户端）。 */
    public static final KeyMapping MAGNIFY_HOLD = new KeyMapping(
            "key.fantasy_furniture.dev_magnify_hold",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.misc");

    private DevMagnifyKeys() {}

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MAGNIFY_HOLD);
    }
}
