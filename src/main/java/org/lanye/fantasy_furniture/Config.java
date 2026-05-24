package org.lanye.fantasy_furniture;

import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

/** 模组通用配置（对应 {@code fantasy_furniture-common.toml}）。 */
public final class Config {

    /** 默认脱离座椅后再次可入座冷却（tick）。 */
    public static final int DEFAULT_SEAT_COOLDOWN_TICKS = 4;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.IntValue SEAT_COOLDOWN_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_PATROL_RADIUS;
    private static final ForgeConfigSpec.IntValue SWEEPER_REENTER_GOAL_RADIUS_BLOCKS;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_PICKUP_FORWARD_REACH;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_PICKUP_ASIDE_REACH;
    private static final ForgeConfigSpec.IntValue SWEEPER_RETURN_HEALTH_THRESHOLD;
    private static final ForgeConfigSpec.IntValue SWEEPER_HEAL_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_DECAY_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue SWEEPER_CACHE_SLOTS;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_MOVE_SPEED;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_TURN_SPEED_DEGREES;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_TURN_THRESHOLD_DEGREES;
    private static final ForgeConfigSpec.IntValue SWEEPER_TURN_PAUSE_TICKS;
    private static final ForgeConfigSpec.DoubleValue SWEEPER_DOCK_REVERSE_RANGE;
    private static final ForgeConfigSpec.BooleanValue SWEEPER_ENABLE_WALL_CLIMB;
    private static final ForgeConfigSpec.IntValue SWEEPER_RETURN_DOCK_INSURANCE_TICKS;
    private static final ForgeConfigSpec.BooleanValue BED_PLATE6_DUVET_COLLISION;
    private static final ForgeConfigSpec.BooleanValue DEVELOPMENT_MODE_ENABLED;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("seat");
        SEAT_COOLDOWN_TICKS =
                b.comment("玩家离开座椅后再次交互入座的冷却时间（tick）。20 tick = 1 秒。")
                        .defineInRange("cooldownTicks", DEFAULT_SEAT_COOLDOWN_TICKS, 0, 20 * 60);
        b.pop();

        b.push("sweeper");
        SWEEPER_PATROL_RADIUS =
                b.comment("扫地机器人以机仓为中心的巡逻半径（方块）。")
                        .defineInRange("patrolRadius", 24, 8, 64);
        SWEEPER_REENTER_GOAL_RADIUS_BLOCKS =
                b.comment(
                                "回巡逻区时目标落点相对机仓的最大水平距离（方块），须不大于 patrolRadius；运行时还会钳到不超过 patrolRadius。")
                        .defineInRange("reenterGoalRadiusBlocks", 10, 2, 64);
        SWEEPER_PICKUP_FORWARD_REACH =
                b.comment(
                                "遗留 TOML 键：当前拾取逻辑严格按机器人 blockPosition() 同格判定，此值仅兼容旧配置，不参与计算。")
                        .defineInRange("pickupForwardReach", 0.5D, 0.15D, 1.5D);
        SWEEPER_PICKUP_ASIDE_REACH =
                b.comment(
                                "遗留 TOML 键：当前拾取逻辑严格按机器人 blockPosition() 同格判定，此值仅兼容旧配置，不参与计算。")
                        .defineInRange("pickupAsideReach", 0.35D, 0.08D, 0.8D);
        SWEEPER_RETURN_HEALTH_THRESHOLD =
                b.comment("生命值严格低于该阈值时，机器人会优先返回机仓（状态机用 `getHealth() < 阈值` 判定）。")
                        .defineInRange("returnHealthThreshold", 5, 2, 10);
        SWEEPER_HEAL_INTERVAL_TICKS =
                b.comment("机器人停靠在机仓时的回血间隔（tick）。")
                        .defineInRange("healIntervalTicks", 20 * 60, 20, 20 * 60 * 60);
        SWEEPER_DECAY_INTERVAL_TICKS =
                b.comment("机器人离开机仓时的掉血间隔（tick）。")
                        .defineInRange("decayIntervalTicks", 20 * 60 * 10, 20, 20 * 60 * 60);
        SWEEPER_CACHE_SLOTS =
                b.comment("扫地机器人内部缓存槽位（9x3，与小箱子一致）。仅影响拾取/卸货逻辑容量，界面固定 27 格。")
                        .defineInRange("cacheSlots", 27, 9, 27);
        SWEEPER_MOVE_SPEED =
                b.comment("机器人每 tick 的前进速度。")
                        .defineInRange("moveSpeed", 0.08D, 0.01D, 0.4D);
        SWEEPER_TURN_SPEED_DEGREES =
                b.comment("机器人每 tick 最大转向角度（度）。")
                        .defineInRange("turnSpeedDegrees", 8.0D, 1.0D, 45.0D);
        SWEEPER_TURN_THRESHOLD_DEGREES =
                b.comment("当目标朝向差值超过该阈值时，机器人会先原地转向再前进。")
                        .defineInRange("turnThresholdDegrees", 10.0D, 1.0D, 90.0D);
        SWEEPER_TURN_PAUSE_TICKS =
                b.comment(
                                "原地转向前后各停顿的 tick 数（20 TPS 下 10 tick 约 0.5 秒）；设为 0 不停顿。")
                        .defineInRange("turnPauseTicks", 10, 0, 40);
        SWEEPER_DOCK_REVERSE_RANGE =
                b.comment(
                                "遗留 TOML 键：低电三段入库链阶段 2 固定倒车直至已入仓，当前逻辑不读取此值；仅兼容旧配置。")
                        .defineInRange("dockReverseRange", 2.5D, 0.5D, 8.0D);
        SWEEPER_ENABLE_WALL_CLIMB =
                b.comment(
                                "是否允许机器人在巡逻/收集状态启用蜘蛛式攀墙；false 表示仅地面移动。")
                        .define("enableWallClimb", true);
        SWEEPER_RETURN_DOCK_INSURANCE_TICKS =
                b.comment(
                                "低血回仓（RETURNING_LOW_HEALTH）期间若持续无法入仓达到该 tick 数，则先在当前位置丢光背包物品再传送到机仓；"
                                        + "防止卡死。20 tick = 1 秒。")
                        .defineInRange("returnDockInsuranceTicks", 20 * 60, 20 * 10, 20 * 60 * 15);
        b.pop();

        b.push("bedPlate6");
        BED_PLATE6_DUVET_COLLISION =
                b.comment(
                                "床板 6 已铺被单时，是否将被单薄层并入方块碰撞盒；false 时实体可穿过该层（默认关闭，仅保留床垫等基座碰撞）。")
                        .define("duvetCollision", false);
        b.pop();

        b.push("development");
        DEVELOPMENT_MODE_ENABLED =
                b.comment(
                                "开发模式：开启后主创造栏显示「变体调试棒」「Gecko 对齐探针」，并允许其放置与交互。"
                                        + "正式游玩请保持 false。")
                        .define("enabled", false);
        b.pop();
        SPEC = b.build();
    }

    private Config() {}

    /** 离开座椅后再次入座的冷却时间（tick）。 */
    public static int seatCooldownTicks() {
        return SEAT_COOLDOWN_TICKS.get();
    }

    /** 机仓周围巡逻半径（方块）。 */
    public static int sweeperPatrolRadius() {
        return SWEEPER_PATROL_RADIUS.get();
    }

    /**
     * 回巡逻区目标相对机仓水平最大距离；运行时钳到不超过 {@link #sweeperPatrolRadius()}。
     */
    public static int sweeperReenterGoalRadiusBlocks() {
        int patrol = sweeperPatrolRadius();
        return Mth.clamp(SWEEPER_REENTER_GOAL_RADIUS_BLOCKS.get(), 2, patrol);
    }

    /** 遗留 TOML 读取接口；吸尘器实体当前按同格方块拾取，不应用此返回值。 */
    public static double sweeperPickupForwardReach() {
        return SWEEPER_PICKUP_FORWARD_REACH.get();
    }

    /** 遗留 TOML 读取接口；吸尘器实体当前按同格方块拾取，不应用此返回值。 */
    public static double sweeperPickupAsideReach() {
        return SWEEPER_PICKUP_ASIDE_REACH.get();
    }

    /** 触发回仓的生命值阈值。 */
    public static int sweeperReturnHealthThreshold() {
        return SWEEPER_RETURN_HEALTH_THRESHOLD.get();
    }

    /** 停靠时回血间隔（tick）。 */
    public static int sweeperHealIntervalTicks() {
        return SWEEPER_HEAL_INTERVAL_TICKS.get();
    }

    /** 离仓时掉血间隔（tick）。 */
    public static int sweeperDecayIntervalTicks() {
        return SWEEPER_DECAY_INTERVAL_TICKS.get();
    }

    /** 机器人内部缓存槽位数量。 */
    public static int sweeperCacheSlots() {
        return SWEEPER_CACHE_SLOTS.get();
    }

    /** 前进速度（每 tick 位移参数）。 */
    public static double sweeperMoveSpeed() {
        return SWEEPER_MOVE_SPEED.get();
    }

    /** 最大转向速度（度/ tick）。 */
    public static float sweeperTurnSpeedDegrees() {
        return SWEEPER_TURN_SPEED_DEGREES.get().floatValue();
    }

    /** 超过该角差时先原地转向（度）。 */
    public static float sweeperTurnThresholdDegrees() {
        return SWEEPER_TURN_THRESHOLD_DEGREES.get().floatValue();
    }

    /** 回仓时允许倒车的半径（方块，距机仓中心）。 */
    public static double sweeperDockReverseRange() {
        return SWEEPER_DOCK_REVERSE_RANGE.get();
    }

    /** 转向前后停顿 tick（0 = 不停）。 */
    public static int sweeperTurnPauseTicks() {
        return SWEEPER_TURN_PAUSE_TICKS.get();
    }

    /** 是否允许扫地机器人启用蜘蛛式攀墙。 */
    public static boolean sweeperEnableWallClimb() {
        return SWEEPER_ENABLE_WALL_CLIMB.get();
    }

    /**
     * 低血回仓卡死保险：累计该 tick 后丢背包并传送入仓。
     */
    public static int sweeperReturnDockInsuranceTicks() {
        return SWEEPER_RETURN_DOCK_INSURANCE_TICKS.get();
    }

    /** 床板 6 被单是否参与碰撞（默认 false）。 */
    public static boolean bedPlate6DuvetCollision() {
        return BED_PLATE6_DUVET_COLLISION.get();
    }

    /** 开发模式：调试棒、对齐探针等（默认 false）。 */
    public static boolean developmentModeEnabled() {
        return DEVELOPMENT_MODE_ENABLED.get();
    }

}
