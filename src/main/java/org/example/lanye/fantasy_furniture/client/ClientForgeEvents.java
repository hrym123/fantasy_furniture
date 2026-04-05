package org.example.lanye.fantasy_furniture.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.example.lanye.fantasy_furniture.network.ModNetwork;
import org.example.lanye.fantasy_furniture.network.StirHoldPacket;
import org.example.lanye.fantasy_furniture.network.StirShortPacket;

/**
 * 客户端：根据使用键 + 准心判定短按/长按，向服务端发送搅拌模式。
 * <p>
 * 放置方块与对已有方块交互在原版中走不同逻辑：{@link PlayerInteractEvent.RightClickBlock} 的坐标在放置时是
 * <strong>被点击的支撑面所在方块</strong>，只有真正对搅拌碗交互时才是碗的位置。本类用该事件标记
 * 「本次按下是否点到了碗」，避免仅靠射线在放置后误指到新碗而误发搅拌包。
 * <ul>
 *   <li>短按：按住时间未达阈值即松开或移开准心 → 播放一次动画
 *   <li>长按：达到阈值后循环，直到松开或准心离开
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Fantasy_furniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {

    /** 达到该 tick 数视为长按并进入循环（约 0.4s @20tps） */
    private static final int LONG_PRESS_TICKS = 8;

    /**
     * 仅用于长按：对准后的前若干 tick 不计入长按累计，减轻放置后误触长按循环（短按仍按原始 tick 判定，保证单击可触发）。
     */
    private static final int LONG_PRESS_LEAD_IN_TICKS = 2;

    private static boolean wasTargetingStir;
    /** 本段对准期间累计 tick（仅当使用键按下且对准搅拌碗时递增） */
    private static int holdTicksWhileTargeting;
    /** 已向服务端声明进入长按循环 */
    private static boolean clientHoldLoopSent;
    private static BlockPos lastBowlPos;

    /**
     * 最近一次 {@link PlayerInteractEvent.RightClickBlock} 是否点在搅拌碗上（与放置时点在别格区分）。
     * 短按搅拌与开始长按循环均要求为 true；结束长按循环仍照常发包。
     */
    private static boolean stirEligibleByVanillaRightClick;

    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getSide().isClient()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) > 10.0 * 10.0) {
            return;
        }
        BlockEntity be = event.getLevel().getBlockEntity(pos);
        stirEligibleByVanillaRightClick = be instanceof MixingBowlBlockEntity;
    }

    /**
     * 对空气 / 非方块面使用物品时清除资格（避免残留）；若主手仍对准碗且按住使用键则不清，以免长按期间副手触发本事件误清标志。
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getSide().isClient()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null && computeStirTargeting(mc)) {
            return;
        }
        stirEligibleByVanillaRightClick = false;
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getSide().isClient()) {
            stirEligibleByVanillaRightClick = false;
        }
    }

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
            int longPressTicks = longPressEffectiveTicks(holdTicksWhileTargeting);
            if (stirEligibleByVanillaRightClick
                    && longPressTicks >= LONG_PRESS_TICKS
                    && !clientHoldLoopSent) {
                ModNetwork.CHANNEL.sendToServer(new StirHoldPacket(pos, true));
                clientHoldLoopSent = true;
            }
        } else {
            if (wasTargetingStir && lastBowlPos != null) {
                if (clientHoldLoopSent) {
                    ModNetwork.CHANNEL.sendToServer(new StirHoldPacket(lastBowlPos, false));
                } else if (stirEligibleByVanillaRightClick
                        && holdTicksWhileTargeting > 0
                        && holdTicksWhileTargeting < LONG_PRESS_TICKS) {
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
        stirEligibleByVanillaRightClick = false;
    }

    /** 仅用于长按判定：去掉对准后的前几 tick，减轻放置后误触长按循环。 */
    private static int longPressEffectiveTicks(int rawHoldTicksWhileTargeting) {
        return Math.max(0, rawHoldTicksWhileTargeting - LONG_PRESS_LEAD_IN_TICKS);
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
