package org.lanye.fantasy_furniture.content.furniture.cabinet;

/**
 * 柜子槽位索引约定。
 *
 * <ul>
 *   <li>存储数组长度固定为 {@link #MAX}（BE 固定长度）
 *   <li>设计格（隔板网格 / Shift 放置）容量为 {@link CabinetKind#slotCount()}
 *   <li>自由堆叠按列密排：槽位 {@code index = level * cols + col}（自下而上 level）
 *   <li>柜子1：cols=1，槽 0..n-1；柜子2：cols=3，列 c 使用 c, c+3, c+6, …
 * </ul>
 */
public final class CabinetSlot {

    /** 足以为高堆预留的存储上限（柜子2：3 列 × 8 层）。 */
    public static final int MAX = 24;

    private CabinetSlot() {}

    public static int clampIndex(int index, int slotCount) {
        if (slotCount <= 0) {
            return 0;
        }
        if (index < 0) {
            return 0;
        }
        if (index >= slotCount) {
            return slotCount - 1;
        }
        return index;
    }
}
