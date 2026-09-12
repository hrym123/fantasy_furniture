package org.lanye.fantasy_furniture.content.furniture.cabinet;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;

/**
 * 柜子1型：仅「两格之间的连接中隔」可单独选中。
 * 点选与描边共用 geo 精确盒；准心方块常是上格时回查下方拥有者。
 */
public final class CabinetJointShelfPick {

    public static final int JOINT_SHELF = 1;

    /**
     * 与 {@code cabinet_1_2x}/{@code 2z} 连接板立方一致：origin=(-6,15,-8) size=(12,2,14)。
     * 北向局部，原点底心。
     */
    private static final AABB JOINT_GEO_LOCAL =
            new AABB(-6.0 / 16.0, 15.0 / 16.0, -8.0 / 16.0, 6.0 / 16.0, 17.0 / 16.0, 6.0 / 16.0);

    public record Hit(BlockPos ownerPos, int shelf, Direction facing) {}

    private CabinetJointShelfPick() {}

    @Nullable
    public static Hit pick(
            LevelReader level,
            BlockPos hitPos,
            BlockState hitState,
            Vec3 eyeWorld,
            Vec3 lookWorld,
            boolean includeAbsent) {
        return pick(level, hitPos, hitState, eyeWorld, lookWorld, null, includeAbsent);
    }

    @Nullable
    public static Hit pick(
            LevelReader level,
            BlockPos hitPos,
            BlockState hitState,
            Vec3 eyeWorld,
            Vec3 lookWorld,
            @Nullable Vec3 hitLocation,
            boolean includeAbsent) {
        if (!(hitState.getBlock() instanceof CabinetBlock cabinet)) {
            return null;
        }
        CabinetKind kind = cabinet.kind();
        Direction facing = hitState.getValue(CabinetBlock.FACING);

        if (kind == CabinetKind.CABINET_1) {
            return pickCabinet1Joint(level, hitPos, facing, eyeWorld, lookWorld, hitLocation);
        }

        var raw = level.getBlockEntity(hitPos);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return null;
        }
        CabinetSegment segment = segmentOf(hitState);
        int shelf =
                kind.pickShelf(
                        hitPos, facing, eyeWorld, lookWorld, be.shelvesMask(), includeAbsent, segment);
        return shelf < 0 ? null : new Hit(hitPos, shelf, facing);
    }

    @Nullable
    private static Hit pickCabinet1Joint(
            LevelReader level,
            BlockPos hitPos,
            Direction facing,
            Vec3 eyeWorld,
            Vec3 lookWorld,
            @Nullable Vec3 hitLocation) {
        Hit best = null;
        double bestScore = Double.MAX_VALUE;

        for (BlockPos owner : new BlockPos[] {hitPos, hitPos.below(), hitPos.above()}) {
            Hit candidate = tryJointAt(level, owner, facing, eyeWorld, lookWorld, hitLocation);
            if (candidate == null) {
                continue;
            }
            double score = scoreHit(candidate, eyeWorld, hitLocation);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private static double scoreHit(Hit hit, Vec3 eyeWorld, @Nullable Vec3 hitLocation) {
        if (hitLocation != null) {
            Vec3 local = CabinetKind.toNorthLocal(hit.ownerPos(), hit.facing(), hitLocation);
            Vec3 c = JOINT_GEO_LOCAL.getCenter();
            return local.distanceToSqr(c);
        }
        return eyeWorld.distanceToSqr(
                hit.ownerPos().getX() + 0.5,
                hit.ownerPos().getY() + 1.0,
                hit.ownerPos().getZ() + 0.5);
    }

    @Nullable
    private static Hit tryJointAt(
            LevelReader level,
            BlockPos ownerPos,
            Direction expectedFacing,
            Vec3 eyeWorld,
            Vec3 lookWorld,
            @Nullable Vec3 hitLocation) {
        BlockState state = level.getBlockState(ownerPos);
        if (!(state.getBlock() instanceof CabinetBlock cabinet) || cabinet.kind() != CabinetKind.CABINET_1) {
            return null;
        }
        if (state.getValue(CabinetBlock.FACING) != expectedFacing) {
            return null;
        }
        CabinetSegment segment = segmentOf(state);
        if (segment != CabinetSegment.BOTTOM && segment != CabinetSegment.MIDDLE) {
            return null;
        }
        if (!(level.getBlockEntity(ownerPos) instanceof CabinetBlockEntity)) {
            return null;
        }

        // 命中点落在 geo 精确盒内（浮点容差，非加厚判定）
        if (hitLocation != null) {
            Vec3 local = CabinetKind.toNorthLocal(ownerPos, expectedFacing, hitLocation);
            if (JOINT_GEO_LOCAL.contains(local.x, local.y, local.z)) {
                return new Hit(ownerPos, JOINT_SHELF, expectedFacing);
            }
        }

        // 准心射线与精确中隔盒求交（可从上/下格命中回查到拥有者）
        Vec3 eye = CabinetKind.toNorthLocal(ownerPos, expectedFacing, eyeWorld);
        Vec3 look = CabinetKind.lookToNorth(expectedFacing, lookWorld);
        double lenSq = look.lengthSqr();
        if (lenSq < 1.0e-8) {
            return null;
        }
        Vec3 dir = look.scale(1.0 / Math.sqrt(lenSq));
        var pt = JOINT_GEO_LOCAL.clip(eye, eye.add(dir.scale(16.0)));
        return pt.isPresent() ? new Hit(ownerPos, JOINT_SHELF, expectedFacing) : null;
    }

    /**
     * 描边用：与 geo 一致，Y 裁到本格 [0,1]（跨格 1px 不画进上格）。
     */
    public static AABB jointOutlineLocal() {
        return new AABB(
                JOINT_GEO_LOCAL.minX,
                JOINT_GEO_LOCAL.minY,
                JOINT_GEO_LOCAL.minZ,
                JOINT_GEO_LOCAL.maxX,
                Math.min(JOINT_GEO_LOCAL.maxY, 1.0),
                JOINT_GEO_LOCAL.maxZ);
    }

    /** @deprecated 勿用加厚盒；请用 {@link #jointOutlineLocal()} / 射线精确盒 */
    @Deprecated
    public static AABB jointPickAabb() {
        return jointOutlineLocal();
    }

    private static CabinetSegment segmentOf(BlockState state) {
        if (state.hasProperty(CabinetBlock.SEGMENT)) {
            return state.getValue(CabinetBlock.SEGMENT);
        }
        return CabinetSegment.ALONE;
    }
}
