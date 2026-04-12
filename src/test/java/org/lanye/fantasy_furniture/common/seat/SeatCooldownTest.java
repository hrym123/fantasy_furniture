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

/** {@link SeatCooldown} 依赖存档 NBT 与 {@link ServerLevel#getGameTime()}，用 Mockito 做最小化桩。 */
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
