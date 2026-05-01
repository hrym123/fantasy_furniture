package org.lanye.fantasy_furniture.content.furniture.decor.block.ornament;

/** 组合摆件底座变体（与资源 basename {@code combined_ornament_base_<id>} 对齐）。 */
public enum OrnamentBaseVariant {
    A("a"),
    B("b");

    private final String id;

    OrnamentBaseVariant(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public OrnamentBaseVariant next() {
        OrnamentBaseVariant[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public static OrnamentBaseVariant fromOrdinal(int ordinal) {
        OrnamentBaseVariant[] v = values();
        if (ordinal < 0 || ordinal >= v.length) {
            return A;
        }
        return v[ordinal];
    }
}
