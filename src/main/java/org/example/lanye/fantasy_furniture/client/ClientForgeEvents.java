package org.example.lanye.fantasy_furniture.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.example.lanye.fantasy_furniture.network.ModNetwork;
import org.example.lanye.fantasy_furniture.network.StirHoldPacket;
import org.example.lanye.fantasy_furniture.network.StirShortPacket;

/**
 * 客户端：根据使用键 + 准心判定短按/长按，向服务端发送搅拌模式。
 * <ul>
 *   <li>短按：按住时间未达阈值即松开或移开准心 → 播放一次动画
 *   <li>长按：达到阈值后循环，直到松开或准心离开
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Fantasy_furniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {

    /** 达到该 tick 数视为长按并进入循环（约 0.4s @20tps） */
    private static final int LONG_PRESS_TICKS = 8;

    private static boolean wasTargetingStir;
    /** 本段对准期间累计 tick（仅当使用键按下且对准搅拌碗时递增） */
    private static int holdTicksWhileTargeting;
    /** 已向服务端声明进入长按循环 */
    private static boolean clientHoldLoopSent;
    private static BlockPos lastBowlPos;

    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            resetLocalState();
            return;
        }
        if (mc.screen != null) {
            if (clientHoldLoopSent && lastBowlPos != null) {
                ModNetwork.CHANNEL.sendToServer(new StirHoldPacket(lastBowlPos, false));
            }
            resetLocalState();
            return;
        }

        boolean targeting = computeStirTargeting(mc);
        if (targeting) {
            BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
            lastBowlPos = pos.immutable();
            holdTicksWhileTargeting++;
            if (holdTicksWhileTargeting >= LONG_PRESS_TICKS && !clientHoldLoopSent) {
                ModNetwork.CHANNEL.sendToServer(new StirHoldPacket(pos, true));
                clientHoldLoopSent = true;
            }
        } else {
            if (wasTargetingStir && lastBowlPos != null) {
                if (clientHoldLoopSent) {
                    ModNetwork.CHANNEL.sendToServer(new StirHoldPacket(lastBowlPos, false));
                } else if (holdTicksWhileTargeting > 0 && holdTicksWhileTargeting < LONG_PRESS_TICKS) {
                    ModNetwork.CHANNEL.sendToServer(new StirShortPacket(lastBowlPos));
                }
            }
            holdTicksWhileTargeting = 0;
            clientHoldLoopSent = false;
            lastBowlPos = null;
        }
        wasTargetingStir = targeting;
    }

    private static void resetLocalState() {
        wasTargetingStir = false;
        holdTicksWhileTargeting = 0;
        clientHoldLoopSent = false;
        lastBowlPos = null;
    }

    private static boolean computeStirTargeting(Minecraft mc) {
        if (!mc.options.keyUse.isDown()) {
            return false;
        }
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof MixingBowlBlockEntity)) {
            return false;
        }
        return mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                <= 10.0 * 10.0;
    }
}
