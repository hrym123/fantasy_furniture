package org.lanye.fantasy_furniture.content.sweeper.ai;

/**
 * 内部路径调优常量（代码内配置，不走 Forge TOML）。
 *
 * <p>用途与 ClientRenderTuning 类似：将易调参的内部常量集中管理，避免散落在业务逻辑中。
 */
public final class InternalPathTuning {
    private InternalPathTuning() {}

    public static final class Sweeper {
        private Sweeper() {}

        /**
         * 机器人碰撞半径（方块）。0 表示按实时碰撞箱宽度自动推导（bbWidth * 0.5）。
         */
        public static final double COLLISION_RADIUS = 0.0D;

        /**
         * 在基础半径上叠加的寻路补偿半径，用于减少墙角卡住。
         */
        public static final double PATH_RADIUS_BONUS = 0.12D;
    }
}
