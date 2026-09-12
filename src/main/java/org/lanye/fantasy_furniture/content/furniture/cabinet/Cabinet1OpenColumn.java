package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.ArrayList;
import java.util.List;
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
 * 柜子1：拆除连接中隔后，上下邻格连成同一开口腔柱，自由叠放跨格计算底标高 / 天花。
 */
public final class Cabinet1OpenColumn {

    public record Aimed(CabinetBlockEntity be, int slot) {}

    private Cabinet1OpenColumn() {}

    /**
     * 含 {@code origin} 的开口腔柱，自下而上。非柜子1或无法解析时仅返回 origin 自身（若有 BE）。
     */
    public static List<CabinetBlockEntity> cellsBottomToTop(LevelReader level, BlockPos origin) {
        List<CabinetBlockEntity> out = new ArrayList<>(CabinetKind.CABINET_1_MAX_STACK);
        BlockState state = level.getBlockState(origin);
        if (!(state.getBlock() instanceof CabinetBlock cabinet)
                || cabinet.kind() != CabinetKind.CABINET_1) {
            if (level.getBlockEntity(origin) instanceof CabinetBlockEntity be) {
                out.add(be);
            }
            return out;
        }
        Direction facing = state.getValue(CabinetBlock.FACING);
        BlockPos bottom = origin;
        while (true) {
            BlockPos below = bottom.below();
            if (!CabinetBlock.areLinkedInStack(level, bottom, below, facing)) {
                break;
            }
            CabinetBlockEntity lower = cabinetBe(level, below);
            if (lower == null || !jointOpen(lower)) {
                break;
            }
            bottom = below;
        }
        BlockPos p = bottom;
        for (int i = 0; i < CabinetKind.CABINET_1_MAX_STACK; i++) {
            CabinetBlockEntity be = cabinetBe(level, p);
            if (be == null) {
                break;
            }
            out.add(be);
            BlockPos above = p.above();
            if (!CabinetBlock.areLinkedInStack(level, p, above, facing) || !jointOpen(be)) {
                break;
            }
            p = above;
        }
        return out;
    }

    /** 下格中隔已拆除（或本段无中隔几何）且仍属底/中段时，与上方邻格连通。 */
    public static boolean jointOpen(CabinetBlockEntity lower) {
        if (lower.kind() != CabinetKind.CABINET_1) {
            return false;
        }
        CabinetSegment segment = lower.segment();
        if (segment != CabinetSegment.BOTTOM && segment != CabinetSegment.MIDDLE) {
            return false;
        }
        return !lower.isShelfActive(CabinetJointShelfPick.JOINT_SHELF);
    }

    public static boolean isOpenColumn(List<CabinetBlockEntity> cells) {
        return cells != null && cells.size() > 1;
    }

    /**
     * 开口腔柱内的槽位姿态（返回值 {@code floorY} 为该 BE 局部坐标）。
     * 单格时退回 {@link CabinetStackFloors} 单柜逻辑。
     */
    public static CabinetStackFloors.SlotPose pose(
            CabinetBlockEntity be, int slot, CabinetStackFloors.HeightFn heights) {
        var level = be.getLevel();
        if (level == null) {
            return CabinetStackFloors.poseSingle(be, slot, heights);
        }
        List<CabinetBlockEntity> cells = cellsBottomToTop(level, be.getBlockPos());
        if (cells.size() <= 1) {
            return CabinetStackFloors.poseSingle(be, slot, heights);
        }
        return poseInColumn(cells, be, slot, heights);
    }

    /** 相对 {@code be} 局部：开口腔柱在 {@code localFloorY} 之上的天花。 */
    public static float ceilingAboveLocal(
            CabinetBlockEntity be, float localFloorY, CabinetStackFloors.HeightFn heights) {
        var level = be.getLevel();
        if (level == null) {
            return CabinetStackFloors.ceilingAboveSingle(be, be.kind(), localFloorY);
        }
        List<CabinetBlockEntity> cells = cellsBottomToTop(level, be.getBlockPos());
        if (cells.size() <= 1) {
            return CabinetStackFloors.ceilingAboveSingle(be, be.kind(), localFloorY);
        }
        double floorWorld = be.getBlockPos().getY() + localFloorY;
        double ceilingWorld = Double.POSITIVE_INFINITY;
        CabinetKind kind = CabinetKind.CABINET_1;
        for (CabinetBlockEntity cell : cells) {
            double base = cell.getBlockPos().getY();
            for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
                if (!cell.isShelfActive(si)) {
                    continue;
                }
                double minY = base + kind.shelfLocalAabb(si).minY;
                if (minY > floorWorld + 1.0e-4) {
                    ceilingWorld = Math.min(ceilingWorld, minY);
                }
            }
        }
        if (!Double.isFinite(ceilingWorld)) {
            CabinetBlockEntity top = cells.get(cells.size() - 1);
            ceilingWorld = top.getBlockPos().getY() + 1.0;
        }
        return (float) (ceilingWorld - be.getBlockPos().getY());
    }

    private static CabinetStackFloors.SlotPose poseInColumn(
            List<CabinetBlockEntity> cells,
            CabinetBlockEntity target,
            int slot,
            CabinetStackFloors.HeightFn heights) {
        CabinetKind kind = CabinetKind.CABINET_1;
        int cols = 1;
        record Key(CabinetBlockEntity be, int level) {}
        List<Key> seq = new ArrayList<>();
        for (CabinetBlockEntity cell : cells) {
            int levels = Math.max(1, cell.maxLevelsPerColumn());
            for (int lvl = 0; lvl < levels; lvl++) {
                seq.add(new Key(cell, lvl));
            }
        }
        int n = seq.size();
        float[] floorWorld = new float[n];
        float[] fitHs = new float[n];
        float[] rendHs = new float[n];

        for (int r = 0; r < n; r++) {
            Key key = seq.get(r);
            int s = kind.slotAt(0, key.level());
            int supporting = kind.shelfSupportingSlot(s);
            double base = key.be().getBlockPos().getY();

            if (supporting >= 0 && key.be().isShelfActive(supporting)) {
                floorWorld[r] = (float) (base + kind.itemFloorY(s));
            } else {
                float resolved = Float.NaN;
                for (int br = r - 1; br >= 0; br--) {
                    Key below = seq.get(br);
                    int belowSlot = kind.slotAt(0, below.level());
                    if (!below.be().getItem(belowSlot).isEmpty()) {
                        resolved = floorWorld[br] + rendHs[br];
                        break;
                    }
                }
                if (Float.isNaN(resolved)) {
                    resolved = (float) (base + key.be().cavityBaseFloorY());
                    for (int br = r - 1; br >= 0; br--) {
                        Key below = seq.get(br);
                        int belowSlot = kind.slotAt(0, below.level());
                        int belowShelf = kind.shelfSupportingSlot(belowSlot);
                        if (belowShelf >= 0 && below.be().isShelfActive(belowShelf)) {
                            resolved = floorWorld[br];
                            break;
                        }
                    }
                }
                floorWorld[r] = resolved;
            }
            fitHs[r] = kind.cavityFit(s).height();
            rendHs[r] = heights.rendered(key.be(), s, fitHs[r]);
        }

        int want = CabinetSlot.clampIndex(slot, target.storageSlotCount());
        int wantLevel = want / cols;
        for (int r = 0; r < n; r++) {
            Key key = seq.get(r);
            if (key.be() == target && key.level() == wantLevel) {
                float local = floorWorld[r] - target.getBlockPos().getY();
                return new CabinetStackFloors.SlotPose(local, fitHs[r], rendHs[r]);
            }
        }
        return CabinetStackFloors.poseSingle(target, slot, heights);
    }

    @Nullable
    public static Aimed pickOccupied(
            LevelReader level, BlockPos hitPos, Direction facing, Vec3 eyeWorld, Vec3 lookWorld) {
        List<CabinetBlockEntity> cells = cellsBottomToTop(level, hitPos);
        Aimed best = null;
        double bestDist = Double.MAX_VALUE;
        for (CabinetBlockEntity cell : cells) {
            int slot =
                    CabinetItemPicks.pickOccupiedSlot(cell, facing, eyeWorld, lookWorld);
            if (slot < 0) {
                continue;
            }
            AABB box = CabinetItemPicks.estimatedNorthLocal(cell, slot);
            if (box == null) {
                continue;
            }
            Vec3 eye = CabinetKind.toNorthLocal(cell.getBlockPos(), facing, eyeWorld);
            Vec3 look = CabinetKind.lookToNorth(facing, lookWorld);
            double lenSq = look.lengthSqr();
            if (lenSq < 1.0e-8) {
                continue;
            }
            Vec3 dir = look.scale(1.0 / Math.sqrt(lenSq));
            var pt = box.clip(eye, eye.add(dir.scale(12.0)));
            if (pt.isEmpty()) {
                continue;
            }
            double d = eye.distanceToSqr(pt.get());
            if (d < bestDist) {
                bestDist = d;
                best = new Aimed(cell, slot);
            }
        }
        return best;
    }

    @Nullable
    public static Aimed topOccupied(
            List<CabinetBlockEntity> cells, float hintWorldY, CabinetStackFloors.HeightFn heights) {
        Aimed top = null;
        float topY = Float.NEGATIVE_INFINITY;
        for (CabinetBlockEntity cell : cells) {
            CabinetKind kind = cell.kind();
            int levels = cell.maxLevelsPerColumn();
            for (int lvl = 0; lvl < levels; lvl++) {
                int s = kind.slotAt(0, lvl);
                if (cell.isEmpty(s)) {
                    continue;
                }
                CabinetStackFloors.SlotPose pose = pose(cell, s, heights);
                float midWorld = cell.getBlockPos().getY() + pose.floorY() + Math.max(0f, pose.renderedH()) * 0.5f;
                if (!sameOpenCavityWorld(cells, hintWorldY, midWorld)) {
                    continue;
                }
                float at = cell.getBlockPos().getY() + pose.floorY() + Math.max(0f, pose.renderedH());
                if (at > topY) {
                    topY = at;
                    top = new Aimed(cell, s);
                }
            }
        }
        return top;
    }

    @Nullable
    public static Aimed nextOpenStackAbove(Aimed aimed, CabinetStackFloors.HeightFn heights) {
        var level = aimed.be().getLevel();
        if (level == null) {
            return null;
        }
        List<CabinetBlockEntity> cells = cellsBottomToTop(level, aimed.be().getBlockPos());
        CabinetKind kind = CabinetKind.CABINET_1;

        record Key(CabinetBlockEntity be, int level) {}
        List<Key> seq = new ArrayList<>();
        int aimedIndex = -1;
        for (CabinetBlockEntity cell : cells) {
            int levels = cell.maxLevelsPerColumn();
            for (int lvl = 0; lvl < levels; lvl++) {
                if (cell == aimed.be() && lvl == aimed.slot()) {
                    aimedIndex = seq.size();
                }
                seq.add(new Key(cell, lvl));
            }
        }
        if (aimedIndex < 0) {
            return null;
        }

        CabinetStackFloors.SlotPose aimedPose = pose(aimed.be(), aimed.slot(), heights);
        float aimedTopWorld =
                aimed.be().getBlockPos().getY()
                        + aimedPose.floorY()
                        + Math.max(0f, aimedPose.renderedH());

        for (int i = aimedIndex + 1; i < seq.size(); i++) {
            Key key = seq.get(i);
            int free = kind.slotAt(0, key.level());
            if (!key.be().isEmpty(free)) {
                continue;
            }
            int freeShelf = kind.shelfSupportingSlot(free);
            if (freeShelf >= 0 && key.be().isShelfActive(freeShelf)) {
                float freeFloorWorld = key.be().getBlockPos().getY() + kind.itemFloorY(free);
                if (freeFloorWorld > aimedTopWorld + 1.0e-3f) {
                    continue;
                }
            }
            CabinetStackFloors.SlotPose freePose = pose(key.be(), free, heights);
            float freeFloorWorld = key.be().getBlockPos().getY() + freePose.floorY();
            if (!sameOpenCavityWorld(cells, aimedTopWorld, freeFloorWorld)) {
                continue;
            }
            return new Aimed(key.be(), free);
        }
        return null;
    }

    private static boolean sameOpenCavityWorld(
            List<CabinetBlockEntity> cells, float y1, float y2) {
        float lo = Math.min(y1, y2);
        float hi = Math.max(y1, y2);
        CabinetKind kind = CabinetKind.CABINET_1;
        for (CabinetBlockEntity cell : cells) {
            double base = cell.getBlockPos().getY();
            for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
                if (!cell.isShelfActive(si)) {
                    continue;
                }
                float minY = (float) (base + kind.shelfLocalAabb(si).minY);
                if (minY > lo + 1.0e-4f && minY < hi - 1.0e-4f) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    private static CabinetBlockEntity cabinetBe(LevelReader level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CabinetBlockEntity be
                && be.kind() == CabinetKind.CABINET_1) {
            return be;
        }
        return null;
    }
}
