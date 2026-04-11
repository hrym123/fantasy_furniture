package org.lanye.fantasy_furniture.common.seat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** 家具入座冷却（脱离后一段时间内不可再交互入座）。 */
public final class SeatCooldown {

    private static final String TAG_COOLDOWN_UNTIL = "FantasyFurnitureSeatCooldownUntil";

    /** 默认 2 秒 @ 20 tick/s */
    public static final int DEFAULT_COOLDOWN_TICKS = 40;

    private SeatCooldown() {}

    public static void setCooldownUntil(ServerPlayer player, long gameTimeTick) {
        player.getPersistentData().putLong(TAG_COOLDOWN_UNTIL, gameTimeTick);
    }

    public static boolean canSit(ServerPlayer player, ServerLevel level) {
        long until = player.getPersistentData().getLong(TAG_COOLDOWN_UNTIL);
        return level.getGameTime() >= until;
    }
}
