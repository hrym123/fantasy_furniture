package org.lanye.fantasy_furniture.content.sweeper.entity;

/**
 * 扫地机高层行为态（与同步数据、存档 {@code SweeperState} 整型 ordinal 对应）。
 *
 * <p>声明顺序与 §5.0.1 行为优先级（行为 1～7）对齐；{@link SweeperRobotEntity} 写入 {@code SweeperStateFormat} ≥ 2 时使用当前序数。
 */
public enum SweeperRobotState {
    /** 行为 1：在机仓内未满电稳态（充电）。 */
    DOCKED,
    /** 行为 2：低电/保命三段回仓。 */
    RETURNING_LOW_HEALTH,
    /** 行为 3：满电出仓。 */
    EXITING_DOCK,
    /** 行为 4：满背包就近卸货（寻路至储存邻格落点，不经三段入仓）。 */
    RETURNING_CACHE_FULL,
    /** 行为 5：收集。 */
    COLLECTING,
    /** 行为 6：回巡逻区。 */
    REENTERING_PATROL,
    /** 过渡态；下一 tick 通常切入 {@link #PATROLLING}。 */
    IDLE,
    /** 行为 7：巡逻。 */
    PATROLLING;

    /**
     * 是否处于低电三段入仓（行为 2）；唯一使用 {@code dockApproachPhase} 0/1/2 的态。
     */
    public boolean isLowHealthDockApproach() {
        return this == RETURNING_LOW_HEALTH;
    }

    /**
     * 低电回仓或满背包卸货态；用于比收集/回巡逻区更高优先的状态排斥（与 §4.0.1 顺位一致）。
     */
    public boolean isReturningForInventoryPressure() {
        return this == RETURNING_LOW_HEALTH || this == RETURNING_CACHE_FULL;
    }

    /** @deprecated 请使用 {@link #isLowHealthDockApproach()} 或 {@link #isReturningForInventoryPressure()}。 */
    @Deprecated
    public boolean isDockingReturnSequence() {
        return isLowHealthDockApproach();
    }

    public static SweeperRobotState byOrdinal(int ordinal) {
        SweeperRobotState[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return IDLE;
        }
        return values[ordinal];
    }

    /**
     * 将格式 1（拆分前）的 {@code SweeperState} 序数映射为当前枚举。
     *
     * <p>旧版单一 {@code RETURNING} 为序数 1；{@code legacyReturningAsCacheFull} 由实体在加载时根据生命与缓存推断。
     */
    public static SweeperRobotState migrateLegacyOrdinal(int legacyOrdinal, boolean legacyReturningAsCacheFull) {
        return switch (legacyOrdinal) {
            case 0 -> DOCKED;
            case 1 -> legacyReturningAsCacheFull ? RETURNING_CACHE_FULL : RETURNING_LOW_HEALTH;
            case 2 -> COLLECTING;
            case 3 -> PATROLLING;
            case 4 -> IDLE;
            case 5 -> EXITING_DOCK;
            case 6 -> REENTERING_PATROL;
            default -> IDLE;
        };
    }
}
