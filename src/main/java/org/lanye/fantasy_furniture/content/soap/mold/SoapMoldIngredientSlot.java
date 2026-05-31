package org.lanye.fantasy_furniture.content.soap.mold;

/** 模具四格原料槽（与 {@link SoapMoldContents} 字段对应）。 */
public enum SoapMoldIngredientSlot {
    LIQUID,
    HONEY,
    WATER,
    PIGMENT;

    public static SoapMoldIngredientSlot fromId(int id) {
        SoapMoldIngredientSlot[] values = values();
        if (id < 0 || id >= values.length) {
            return LIQUID;
        }
        return values[id];
    }
}
