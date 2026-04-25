package org.lanye.fantasy_furniture.block.ornament;

/** 组合摆件玩偶变体（与资源 basename {@code combined_ornament_figurine_<id>} 对齐）。 */
public enum OrnamentFigurineVariant {
    A("a"),
    B("b");

    private final String id;

    OrnamentFigurineVariant(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public OrnamentFigurineVariant next() {
        OrnamentFigurineVariant[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public static OrnamentFigurineVariant fromOrdinal(int ordinal) {
        OrnamentFigurineVariant[] v = values();
        if (ordinal < 0 || ordinal >= v.length) {
            return A;
        }
        return v[ordinal];
    }
}
