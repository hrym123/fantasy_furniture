package org.lanye.fantasy_furniture.content.furniture.cabinet;

import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

/**
 * 同列堆叠底标高 / 可用腔高：隔板在则贴层板，隔板拆掉则落在下方展品或下层板顶。
 *
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
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, kind.slotCount());
        int col = kind.colOf(i);
        int rows = kind.rows();
        int cols = kind.cols();

        float[] floors = new float[rows];
        float[] fitHs = new float[rows];
        float[] rendHs = new float[rows];

        for (int r = 0; r < rows; r++) {
            int s = r * cols + col;
            int supporting = kind.shelfSupportingSlot(s);

            if (supporting >= 0 && be.isShelfPresent(supporting)) {
                floors[r] = kind.itemFloorY(s);
            } else {
                float resolved = Float.NaN;
                for (int br = r - 1; br >= 0; br--) {
                    int below = br * cols + col;
                    if (!be.getItem(below).isEmpty()) {
                        resolved = floors[br] + rendHs[br];
                        break;
                    }
                }
                if (Float.isNaN(resolved)) {
                    resolved = kind.itemFloorY(col);
                    for (int br = r - 1; br >= 0; br--) {
                        int below = br * cols + col;
                        int belowShelf = kind.shelfSupportingSlot(below);
                        if (belowShelf >= 0 && be.isShelfPresent(belowShelf)) {
                            resolved = kind.itemFloorY(below);
                            break;
                        }
                    }
                }
                floors[r] = resolved;
            }

            float ceiling = ceilingAbove(be, kind, floors[r]);
            float rawH = Math.max(0.05f, ceiling - floors[r] - CabinetKind.SHELF_CLEARANCE);
            fitHs[r] = rawH * CabinetKind.INTERIOR_FIT;

            if (supporting >= 0 && be.isShelfPresent(supporting)) {
                fitHs[r] = kind.cavityFit(s).height();
            }

            rendHs[r] = heights.rendered(be, s, fitHs[r]);
        }

        int row = kind.rowOf(i);
        return new SlotPose(floors[row], fitHs[row], rendHs[row]);
    }

    public static float floorY(CabinetBlockEntity be, int slot, HeightFn heights) {
        return pose(be, slot, heights).floorY();
    }

    /** 下一层现存隔板底面，否则顶盖。 */
    public static float ceilingAbove(CabinetBlockEntity be, CabinetKind kind, float floorY) {
        float ceiling = (float) kind.shelfLocalAabb(CabinetKind.SHELF_COUNT - 1).minY;
        for (int si = 0; si < CabinetKind.SHELF_COUNT; si++) {
            if (!be.isShelfPresent(si)) {
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
