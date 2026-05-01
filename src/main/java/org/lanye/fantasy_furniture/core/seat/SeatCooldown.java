package org.lanye.fantasy_furniture.core.seat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.lanye.fantasy_furniture.Config;

/** 家具入座冷却（脱离后一段时间内不可再交互入座）。 */
public final class SeatCooldown {

    private static final String TAG_COOLDOWN_UNTIL = "FantasyFurnitureSeatCooldownUntil";

    /** 默认 2 秒 @ 20 tick/s（兜底值，正常由 {@link Config#seatCooldownTicks()} 提供）。 */
    public static final int DEFAULT_COOLDOWN_TICKS = Config.DEFAULT_SEAT_COOLDOWN_TICKS;

    private SeatCooldown() {}

    public static void setCooldownUntil(ServerPlayer player, long gameTimeTick) {
        player.getPersistentData().putLong(TAG_COOLDOWN_UNTIL, gameTimeTick);
    }

    /**
     * 纯逻辑：当前游戏刻是否已到达或超过冷却结束刻（与 {@link #canSit(ServerPlayer, ServerLevel)} 判定一致，便于 JUnit）。
     */
    public static boolean isPastCooldown(long gameTime, long cooldownUntilTick) {
        return gameTime >= cooldownUntilTick;
    }

    public static boolean canSit(ServerPlayer player, ServerLevel level) {
        long until = player.getPersistentData().getLong(TAG_COOLDOWN_UNTIL);
        return isPastCooldown(level.getGameTime(), until);
    }
}
