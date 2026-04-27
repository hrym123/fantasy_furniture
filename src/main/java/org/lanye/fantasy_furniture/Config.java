package org.lanye.fantasy_furniture;

import net.minecraftforge.common.ForgeConfigSpec;

/** 模组通用配置（对应 {@code fantasy_furniture-common.toml}）。 */
public final class Config {

    /** 默认脱离座椅后再次可入座冷却（tick）。 */
    public static final int DEFAULT_SEAT_COOLDOWN_TICKS = 4;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue SEAT_COOLDOWN_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_PATROL_RADIUS;
    private static final ForgeConfigSpec.IntValue SWEEPER_COLLECT_RANGE;
    private static final ForgeConfigSpec.IntValue SWEEPER_RETURN_HEALTH_THRESHOLD;
    private static final ForgeConfigSpec.IntValue SWEEPER_HEAL_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_DECAY_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_CACHE_SLOTS;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_MOVE_SPEED;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_TURN_SPEED_DEGREES;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_TURN_THRESHOLD_DEGREES;
    private static final ForgeConfigSpec.IntValue SWEEPER_PATROL_RANDOM_TURN_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_TURN_PAUSE_TICKS;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_DOCK_REVERSE_RANGE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("seat");
        SEAT_COOLDOWN_TICKS =
                b.comment("Seat interaction cooldown after dismount, in ticks. 20 ticks = 1 second.")
                        .defineInRange("cooldownTicks", DEFAULT_SEAT_COOLDOWN_TICKS, 0, 20 * 60);
        b.pop();

        b.push("sweeper");
        SWEEPER_PATROL_RADIUS =
                b.comment("Patrol radius around dock in blocks.")
                        .defineInRange("patrolRadius", 24, 8, 64);
        SWEEPER_COLLECT_RANGE =
                b.comment("Item collect scan range in blocks.")
                        .defineInRange("collectRange", 5, 2, 10);
        SWEEPER_RETURN_HEALTH_THRESHOLD =
                b.comment("Return-to-dock health threshold.")
                        .defineInRange("returnHealthThreshold", 5, 2, 10);
        SWEEPER_HEAL_INTERVAL_TICKS =
                b.comment("Heal interval while docked, in ticks.")
                        .defineInRange("healIntervalTicks", 20 * 60, 20, 20 * 60 * 60);
        SWEEPER_DECAY_INTERVAL_TICKS =
                b.comment("Health decay interval while undocked, in ticks.")
                        .defineInRange("decayIntervalTicks", 20 * 60 * 10, 20, 20 * 60 * 60);
        SWEEPER_CACHE_SLOTS =
                b.comment("Sweeper robot internal slots (9×3，与小箱子一致). 仅影响拾取/卸货逻辑容量，界面固定 27 格。")
                        .defineInRange("cacheSlots", 27, 9, 27);
        SWEEPER_MOVE_SPEED =
                b.comment("Forward move speed per tick.")
                        .defineInRange("moveSpeed", 0.08D, 0.01D, 0.4D);
        SWEEPER_TURN_SPEED_DEGREES =
                b.comment("Max turn degrees per tick.")
                        .defineInRange("turnSpeedDegrees", 8.0D, 1.0D, 45.0D);
        SWEEPER_TURN_THRESHOLD_DEGREES =
                b.comment("If yaw difference exceeds this threshold, robot only rotates.")
                        .defineInRange("turnThresholdDegrees", 10.0D, 1.0D, 90.0D);
        SWEEPER_PATROL_RANDOM_TURN_INTERVAL_TICKS =
                b.comment(
                                "While PATROLLING, pick a random heading every this many ticks (stop, rotate, then move). 100 = 5s. Set to 0 to disable.")
                        .defineInRange("patrolRandomTurnIntervalTicks", 20 * 5, 0, 20 * 120);
        SWEEPER_TURN_PAUSE_TICKS =
                b.comment(
                                "Before and after in-place turns, pause this many ticks (~0.5s = 10 at 20 TPS). 0 disables pauses.")
                        .defineInRange("turnPauseTicks", 10, 0, 40);
        SWEEPER_DOCK_REVERSE_RANGE =
                b.comment(
                                "When RETURNING and within this many blocks of dock center, allow driving backward (model tail toward dock) to align with 入库.")
                        .defineInRange("dockReverseRange", 2.5D, 0.5D, 8.0D);
        b.pop();
        SPEC = b.build();
    }

    private Config() {}

    public static int seatCooldownTicks() {
        return SEAT_COOLDOWN_TICKS.get();
    }

    public static int sweeperPatrolRadius() {
        return SWEEPER_PATROL_RADIUS.get();
    }

    public static int sweeperCollectRange() {
        return SWEEPER_COLLECT_RANGE.get();
    }

    public static int sweeperReturnHealthThreshold() {
        return SWEEPER_RETURN_HEALTH_THRESHOLD.get();
    }

    public static int sweeperHealIntervalTicks() {
        return SWEEPER_HEAL_INTERVAL_TICKS.get();
    }

    public static int sweeperDecayIntervalTicks() {
        return SWEEPER_DECAY_INTERVAL_TICKS.get();
    }

    public static int sweeperCacheSlots() {
        return SWEEPER_CACHE_SLOTS.get();
    }

    public static double sweeperMoveSpeed() {
        return SWEEPER_MOVE_SPEED.get();
    }

    public static float sweeperTurnSpeedDegrees() {
        return SWEEPER_TURN_SPEED_DEGREES.get().floatValue();
    }

    public static float sweeperTurnThresholdDegrees() {
        return SWEEPER_TURN_THRESHOLD_DEGREES.get().floatValue();
    }

    /** 巡逻时随机换向间隔（tick）；0 表示关闭。 */
    public static int sweeperPatrolRandomTurnIntervalTicks() {
        return SWEEPER_PATROL_RANDOM_TURN_INTERVAL_TICKS.get();
    }

    /** 回仓时允许倒车的半径（方块，距机仓中心）。 */
    public static double sweeperDockReverseRange() {
        return SWEEPER_DOCK_REVERSE_RANGE.get();
    }

    /** 转向前后停顿 tick（0 = 不停）。 */
    public static int sweeperTurnPauseTicks() {
        return SWEEPER_TURN_PAUSE_TICKS.get();
    }
}
