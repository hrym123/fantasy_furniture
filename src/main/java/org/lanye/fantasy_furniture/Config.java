package org.lanye.fantasy_furniture;

import net.minecraftforge.common.ForgeConfigSpec;

/** 模组通用配置（对应 {@code fantasy_furniture-common.toml}）。 */
public final class Config {

    /** 默认脱离座椅后再次可入座冷却（tick）。 */
    public static final int DEFAULT_SEAT_COOLDOWN_TICKS = 4;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue SEAT_COOLDOWN_TICKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("seat");
        SEAT_COOLDOWN_TICKS =
                b.comment("Seat interaction cooldown after dismount, in ticks. 20 ticks = 1 second.")
                        .defineInRange("cooldownTicks", DEFAULT_SEAT_COOLDOWN_TICKS, 0, 20 * 60);
        b.pop();
        SPEC = b.build();
    }

    private Config() {}

    public static int seatCooldownTicks() {
        return SEAT_COOLDOWN_TICKS.get();
    }
}
