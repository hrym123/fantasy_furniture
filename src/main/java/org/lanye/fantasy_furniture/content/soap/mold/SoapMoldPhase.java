package org.lanye.fantasy_furniture.content.soap.mold;

/** 肥皂模具制皂阶段（见 {@code soap_mold.gameplay.md}）。 */
public enum SoapMoldPhase {
    EMPTY,
    FILLING,
    READY_TO_MIX,
    CURING,
    READY;

    public boolean canModifyIngredients() {
        return this == EMPTY || this == FILLING || this == READY_TO_MIX;
    }

    public static SoapMoldPhase fromId(int id) {
        SoapMoldPhase[] values = values();
        if (id < 0 || id >= values.length) {
            return EMPTY;
        }
        return values[id];
    }
}
