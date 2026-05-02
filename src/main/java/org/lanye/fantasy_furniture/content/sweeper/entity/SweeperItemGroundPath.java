package org.lanye.fantasy_furniture.content.sweeper.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 收集用地面路径：在保留原版 {@link Path}（整数 {@link Node}）的前提下，额外保存掉落物世界坐标的完整精度，
 * 供最后一档航点使用；中间节点仍按格点中心跟随。
 */
public final class SweeperItemGroundPath extends Path {
    private final Vec3 exactItemPosition;

    public SweeperItemGroundPath(
            List<Node> nodes, BlockPos targetBlock, boolean canReach, Vec3 exactItemPosition) {
        super(nodes, targetBlock, canReach);
        this.exactItemPosition = exactItemPosition;
    }

    /** 掉落物实体在这一瞬间的世界坐标（与求路时传入的 {@link Vec3} 分量一致）。 */
    public Vec3 exactItemPosition() {
        return this.exactItemPosition;
    }
}
