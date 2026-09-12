package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;

/**
 * Shift+RMB on a cabinet must run {@link CabinetBlock} use logic and must not place the held
 * block in the world. Vanilla skips {@code Block#use} when sneaking with a non-empty hand, so
 * Forge must force use-block and deny use-item (client predict + server).
 */
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CabinetInteractEvents {

    private CabinetInteractEvents() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return;
        }
        // Consume item/block-place path; still invoke CabinetBlock#use (take / grid place).
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.ALLOW);
    }
}
