package org.lanye.fantasy_furniture.common.seat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 冷却判定只测 {@link SeatCooldown#isPastCooldown(long, long)}：{@link SeatCooldown#canSit} 依赖
 * {@link net.minecraft.server.level.ServerPlayer} / NBT，在 JUnit 用 Mockito 打桩会触发实体子系统静态初始化失败。
 */
class SeatCooldownTest {

    @Test
    void pastCooldown_whenGameTimeAfterEnd() {
        assertTrue(SeatCooldown.isPastCooldown(101L, 100L));
    }

    @Test
    void pastCooldown_whenGameTimeEqualsEnd() {
        assertTrue(SeatCooldown.isPastCooldown(100L, 100L));
    }

    @Test
    void notPastCooldown_whenBeforeEnd() {
        assertFalse(SeatCooldown.isPastCooldown(99L, 100L));
    }

    @Test
    void pastCooldown_whenNeverHadTagEffectivelyZero() {
        assertTrue(SeatCooldown.isPastCooldown(0L, 0L));
    }
}
