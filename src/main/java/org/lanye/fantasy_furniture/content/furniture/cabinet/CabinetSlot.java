package org.lanye.fantasy_furniture.content.furniture.cabinet;

/**
 * 柜内槽位索引约定。
 *
 * <ul>
 *   <li>存储容量上限 {@link #MAX}（BE 固定长度）
 *   <li>实际可用数见 {@link CabinetKind#slotCount()}
 *   <li>柜子1：{@code index = row}（自下而上 0..2）
 *   <li>柜子2：{@code index = row * 3 + col}（row 自下而上，col 自左向右，北向模型空间）
 * </ul>
 */
public final class CabinetSlot {

    public static final int MAX = 9;

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
