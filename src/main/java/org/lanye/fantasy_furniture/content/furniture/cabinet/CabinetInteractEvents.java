package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;

/**
 * 潜行右击柜子时，原版会跳过 {@code Block#use} 而走物品放置。
 * 需强制柜交互的场合：取出展品、或持非方块放入展品；潜行持方块且未对准展品则放行原版放置。
 */
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CabinetInteractEvents {

    private CabinetInteractEvents() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof BlockItem) {
            // 对准展品 → 仍强制取出；否则不拦截，走原版方块放置
            if (!CabinetBlock.hasAimedExhibit(level, state, pos, player)) {
                return;
            }
        }
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.ALLOW);
    }
}
