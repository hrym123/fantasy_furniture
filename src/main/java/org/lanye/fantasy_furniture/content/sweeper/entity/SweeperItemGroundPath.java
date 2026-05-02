package org.lanye.fantasy_furniture.content.sweeper.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 收集用地面路径：在保留原版 {@link Path}（整数 {@link Node}）的前提下，额外保存 **末档航点**（通常为路径终点
 * 方块 {@link Vec3#atCenterOf(BlockPos)}）；中间节点由实体侧按格点中心跟随。
 */
public final class SweeperItemGroundPath extends Path {
    private final Vec3 exactItemPosition;

    public SweeperItemGroundPath(
            List<Node> nodes, BlockPos targetBlock, boolean canReach, Vec3 exactItemPosition) {
        super(nodes, targetBlock, canReach);
        this.exactItemPosition = exactItemPosition;
    }

    /** 末档驱动目标（一般为路径终点方块中心，见 {@link SweeperGroundNavigation#createPathToExactPos}）。 */
    public Vec3 exactItemPosition() {
        return this.exactItemPosition;
    }
}
