package org.lanye.fantasy_furniture.content.furniture.cabinet;

import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

/**
 * 同列堆叠底标高 / 可用腔高：隔板在则贴层板，隔板拆掉则落在下方展品或下层板顶。
 *
 * <p>柜子1 开口腔柱（拆中隔）委托 {@link Cabinet1OpenColumn}。
 * <p>高度回调由调用方提供：客户端用模型实测（与 BER 一致），服务端用占位估计。
 */
public final class CabinetStackFloors {

    @FunctionalInterface
    public interface HeightFn {
        /** 该槽在给定堆叠腔高下的缩放后高度；空槽为 0。 */
        float rendered(CabinetBlockEntity be, int slot, float fitH);
    }

    public record SlotPose(float floorY, float fitH, float renderedH) {}

    private CabinetStackFloors() {}

    public static SlotPose pose(CabinetBlockEntity be, int slot, HeightFn heights) {
        if (be.kind() == CabinetKind.CABINET_1) {
            return Cabinet1OpenColumn.pose(be, slot, heights);
        }
        return poseSingle(be, slot, heights);
    }

    /** 单柜（或柜子2）密排姿态；柜子1 开口腔请走 {@link #pose}。 */
    public static SlotPose poseSingle(CabinetBlockEntity be, int slot, HeightFn heights) {
        CabinetKind kind = be.kind();
        int cols = Math.max(1, kind.cols());
        int levels = Math.max(1, be.maxLevelsPerColumn());
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        int col = kind.columnOfStorage(i);
        int levelOfSlot = i / cols;

        float[] floors = new float[levels];
        float[] fitHs = new float[levels];
        float[] rendHs = new float[levels];

        for (int r = 0; r < levels; r++) {
            int s = kind.slotAt(col, r);
            int supporting = kind.shelfSupportingSlot(s);

            if (supporting >= 0 && be.isShelfActive(supporting)) {
                floors[r] = kind.itemFloorY(s);
            } else {
                float resolved = Float.NaN;
                for (int br = r - 1; br >= 0; br--) {
                    int below = kind.slotAt(col, br);
                    if (!be.getItem(below).isEmpty()) {
                        resolved = floors[br] + rendHs[br];
                        break;
                    }
                }
                if (Float.isNaN(resolved)) {
                    resolved = be.cavityBaseFloorY();
                    for (int br = r - 1; br >= 0; br--) {
                        int below = kind.slotAt(col, br);
                        int belowShelf = kind.shelfSupportingSlot(below);
                        if (belowShelf >= 0 && be.isShelfActive(belowShelf)) {
                            resolved = kind.itemFloorY(below);
                            break;
                        }
                    }
                }
                floors[r] = resolved;
            }

            // Display scale is always the design cell (same as a shelved single compartment).
            // Remaining height to the next shelf/lid is a placement budget only — never a shrink target.
            fitHs[r] = kind.cavityFit(s).height();

            rendHs[r] = heights.rendered(be, s, fitHs[r]);
        }

        int lvl = Math.min(levelOfSlot, levels - 1);
        return new SlotPose(floors[lvl], fitHs[lvl], rendHs[lvl]);
    }

    public static float floorY(CabinetBlockEntity be, int slot, HeightFn heights) {
        return pose(be, slot, heights).floorY();
    }

    /** 下一层现存且对本段有效的隔板底面，否则顶盖（柜子1 开口腔柱见 {@link Cabinet1OpenColumn}）。 */
    public static float ceilingAbove(CabinetBlockEntity be, CabinetKind kind, float floorY) {
        if (kind == CabinetKind.CABINET_1) {
            return Cabinet1OpenColumn.ceilingAboveLocal(be, floorY, (b, s, h) -> 0f);
        }
        return ceilingAboveSingle(be, kind, floorY);
    }

    public static float ceilingAboveSingle(CabinetBlockEntity be, CabinetKind kind, float floorY) {
        float ceiling = (float) kind.shelfLocalAabb(CabinetKind.SHELF_COUNT - 1).minY;
        for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
            if (!be.isShelfActive(si)) {
                continue;
            }
            float minY = (float) kind.shelfLocalAabb(si).minY;
            if (minY > floorY + 1.0e-4f) {
                return minY;
            }
        }
        return ceiling;
    }
}
