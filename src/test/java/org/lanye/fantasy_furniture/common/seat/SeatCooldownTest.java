package org.lanye.fantasy_furniture.common.seat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 验证 {@link SeatCooldown}：根据玩家持久化数据与当前游戏时间，能否入座、冷却是否按预期生效。
 * <p>
 * {@link ServerPlayer} / {@link ServerLevel} 为 Mockito 桩，仅打桩 {@link ServerLevel#getGameTime()} 等与冷却相关的调用。
 */
@ExtendWith(MockitoExtension.class)
class SeatCooldownTest {

    @Mock
    private ServerPlayer player;

    @Mock
    private ServerLevel level;

    @Test
    void canSit_whenNoCooldown() {
        when(level.getGameTime()).thenReturn(100L);
        when(player.getPersistentData()).thenReturn(new net.minecraft.nbt.CompoundTag());

        assertTrue(SeatCooldown.canSit(player, level));
    }

    @Test
    void cannotSit_beforeCooldownExpires() {
        when(level.getGameTime()).thenReturn(99L);
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putLong("FantasyFurnitureSeatCooldownUntil", 100L);
        when(player.getPersistentData()).thenReturn(tag);

        assertFalse(SeatCooldown.canSit(player, level));
    }

    @Test
    void canSit_afterCooldownExpires() {
        when(level.getGameTime()).thenReturn(100L);
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putLong("FantasyFurnitureSeatCooldownUntil", 100L);
        when(player.getPersistentData()).thenReturn(tag);

        assertTrue(SeatCooldown.canSit(player, level));
    }
}
