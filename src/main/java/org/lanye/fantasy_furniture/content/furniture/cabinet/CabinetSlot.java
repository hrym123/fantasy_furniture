package org.lanye.fantasy_furniture.content.furniture.cabinet;

/** 柜子三层展示槽（自下而上）。 */
public enum CabinetSlot {
    BOTTOM(0),
    MIDDLE(1),
    TOP(2);

    public static final int COUNT = 3;

    private final int index;

    CabinetSlot(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    public static CabinetSlot byIndex(int index) {
        return switch (index) {
            case 1 -> MIDDLE;
            case 2 -> TOP;
            default -> BOTTOM;
        };
    }
}
