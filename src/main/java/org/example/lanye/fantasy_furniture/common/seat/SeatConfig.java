package org.example.lanye.fantasy_furniture.common.seat;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 可入座家具的配置：支撑方块判定、可入座空间（相对方块）、坐骑实体位置与朝向。
 * <p>
 * <strong>方块相对坐标</strong>：原点为方块最小角 {@code (blockX, blockY, blockZ)}，轴向与世界一致，
 * {@code sitRangeBlockRelative} 的各分量在 {@code [0,1]} 表示该轴在方块内的比例（与 {@code Block.box} 一致）。
 */
public record SeatConfig(
        Predicate<BlockState> blockValid,
        AABB sitRangeBlockRelative,
        Vec3 seatEntityOffsetFromBlockMin,
        Function<BlockState, Float> yawDegrees) {

    /** 将「方块内」入座范围转为世界坐标 AABB，用于与玩家 {@link net.minecraft.world.entity.Entity#getBoundingBox()} 相交检测。 */
    public AABB toWorldSitRange(BlockPos pos) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        return new AABB(
                x + sitRangeBlockRelative.minX,
                y + sitRangeBlockRelative.minY,
                z + sitRangeBlockRelative.minZ,
                x + sitRangeBlockRelative.maxX,
                y + sitRangeBlockRelative.maxY,
                z + sitRangeBlockRelative.maxZ);
    }

    /** 坐骑实体中心位置（与原版实体坐标一致）。 */
    public Vec3 seatWorldPosition(BlockPos anchor) {
        return new Vec3(
                anchor.getX() + seatEntityOffsetFromBlockMin.x,
                anchor.getY() + seatEntityOffsetFromBlockMin.y,
                anchor.getZ() + seatEntityOffsetFromBlockMin.z);
    }
}
