package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

/**
 * Per-column stack layout when supporting shelves are removed.
 *
 * <p>Bottom→top: each slot's floor sits on its shelf, or on the item/shelf below;
 * fit height reaches the next present shelf underside (or lid).
 */
@OnlyIn(Dist.CLIENT)
final class CabinetStackLayout {

    private CabinetStackLayout() {}

    static float floorY(CabinetBlockEntity be, int slot) {
        return layout(be, slot).floorY;
    }

    static CabinetKind.CavityFit cavityFitStacked(CabinetBlockEntity be, int slot) {
        SlotLayout s = layout(be, slot);
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, kind.slotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        return new CabinetKind.CavityFit(base.width(), s.fitH, base.depth());
    }

    /**
     * Scaled AABB height matching {@link CabinetDisplayedItemRenderer#draw} for this slot's
     * current stack fit. Empty → 0.
     */
    static float renderedHeight(CabinetBlockEntity be, int slot, ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty()) {
            return 0f;
        }
        SlotLayout s = layout(be, slot);
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, kind.slotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        return CabinetDisplayedItemRenderer.measureRenderedHeight(
                stack, level, base.width(), s.fitH, base.depth());
    }

    private static SlotLayout layout(CabinetBlockEntity be, int slot) {
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, kind.slotCount());
        int col = kind.colOf(i);
        int rows = kind.rows();
        int cols = kind.cols();
        Level level = be.getLevel();

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
                    // Sit on nearest lower present shelf, else cabinet bottom (row 0).
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

            // When the supporting shelf is present, keep the original per-row cavity height
            // so restored shelves match prior visuals exactly.
            if (supporting >= 0 && be.isShelfPresent(supporting)) {
                fitHs[r] = kind.cavityFit(s).height();
            }

            ItemStack stack = be.getItem(s);
            if (stack.isEmpty() || level == null) {
                rendHs[r] = 0f;
            } else {
                CabinetKind.CavityFit base = kind.cavityFit(s);
                rendHs[r] = CabinetDisplayedItemRenderer.measureRenderedHeight(
                        stack, level, base.width(), fitHs[r], base.depth());
            }
        }

        int row = kind.rowOf(i);
        return new SlotLayout(floors[row], fitHs[row], rendHs[row]);
    }

    /** Underside of the next present shelf above {@code floorY}, else lid / interior ceiling. */
    private static float ceilingAbove(CabinetBlockEntity be, CabinetKind kind, float floorY) {
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

    private record SlotLayout(float floorY, float fitH, float renderedH) {}
}
