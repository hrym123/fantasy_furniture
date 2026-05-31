package org.lanye.fantasy_furniture.content.soap.mold;

/** 制皂液体原料种类（沐浴液 / 洗发液物品，非 Geo 瓶）。 */
public enum SoapMoldLiquidKind {
    NONE,
    BODY_WASH,
    SHAMPOO;

    public static SoapMoldLiquidKind fromId(int id) {
        SoapMoldLiquidKind[] values = values();
        if (id < 0 || id >= values.length) {
            return NONE;
        }
        return values[id];
    }
}
