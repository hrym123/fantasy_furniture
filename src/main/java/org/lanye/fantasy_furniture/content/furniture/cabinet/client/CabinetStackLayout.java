package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetStackFloors;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

/**
 * Per-column stack layout when supporting shelves are removed.
 *
 * <p>Bottom→top: each slot's floor sits on its shelf, or on the item/shelf below.
 * Display fit W/H/D is the design cell (same as a shelved single compartment),
 * not remaining-to-lid.
 */
@OnlyIn(Dist.CLIENT)
final class CabinetStackLayout {

    private CabinetStackLayout() {}

    static float floorY(CabinetBlockEntity be, int slot) {
        return pose(be, slot).floorY();
    }

    static CabinetKind.CavityFit cavityFitStacked(CabinetBlockEntity be, int slot) {
        CabinetStackFloors.SlotPose s = pose(be, slot);
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        return new CabinetKind.CavityFit(base.width(), s.fitH(), base.depth());
    }

    /**
     * Scaled AABB height matching {@link CabinetDisplayedItemRenderer#draw} for this slot's
     * current stack fit. Empty -> 0.
     */
    static float renderedHeight(CabinetBlockEntity be, int slot, ItemStack stack, Level level) {
        CabinetItemPicks.Size size = renderedSize(be, slot, stack, level);
        return size.height();
    }

    static CabinetItemPicks.Size renderedSize(CabinetBlockEntity be, int slot, ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty() || level == null) {
            return new CabinetItemPicks.Size(0f, 0f, 0f);
        }
        CabinetStackFloors.SlotPose s = pose(be, slot);
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        return CabinetDisplayedItemRenderer.measureRenderedSize(
                stack, level, base.width(), s.fitH(), base.depth());
    }

    private static CabinetStackFloors.SlotPose pose(CabinetBlockEntity be, int slot) {
        return CabinetStackFloors.pose(be, slot, CabinetStackLayout::measuredHeight);
    }

    private static float measuredHeight(CabinetBlockEntity be, int slot, float fitH) {
        ItemStack stack = be.getItem(slot);
        Level level = be.getLevel();
        if (stack.isEmpty() || level == null) {
            return 0f;
        }
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        return CabinetDisplayedItemRenderer.measureRenderedHeight(
                stack, level, base.width(), fitH, base.depth());
    }
}
