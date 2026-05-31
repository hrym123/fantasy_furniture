package org.lanye.fantasy_furniture.content.soap.mold;

/** 肥皂模具 BE 快照（原料 + 阶段 + 凝固计时）。 */
public record SoapMoldContents(
        SoapMoldPhase phase,
        SoapMoldLiquidKind liquidKind,
        int liquidMatId,
        boolean hasHoneycomb,
        boolean hasWater,
        int pigmentMatId,
        long cureFinishGameTime) {

    public static SoapMoldContents empty() {
        return new SoapMoldContents(
                SoapMoldPhase.EMPTY,
                SoapMoldLiquidKind.NONE,
                0,
                false,
                false,
                0,
                0L);
    }

    public boolean hasLiquid() {
        return liquidKind != SoapMoldLiquidKind.NONE && liquidMatId >= 1;
    }

    public boolean hasPigment() {
        return pigmentMatId >= 1;
    }

    public int filledCount() {
        int count = 0;
        if (hasLiquid()) {
            count++;
        }
        if (hasHoneycomb) {
            count++;
        }
        if (hasWater) {
            count++;
        }
        if (hasPigment()) {
            count++;
        }
        return count;
    }

    public boolean isFull() {
        return filledCount() == 4;
    }

    public SoapMoldContents withPhase(SoapMoldPhase next) {
        return new SoapMoldContents(
                next, liquidKind, liquidMatId, hasHoneycomb, hasWater, pigmentMatId, cureFinishGameTime);
    }

    public SoapMoldContents recomputeIngredientPhase() {
        if (phase == SoapMoldPhase.CURING || phase == SoapMoldPhase.READY) {
            return this;
        }
        int filled = filledCount();
        if (filled == 0) {
            return withPhase(SoapMoldPhase.EMPTY);
        }
        if (isFull()) {
            return withPhase(SoapMoldPhase.READY_TO_MIX);
        }
        return withPhase(SoapMoldPhase.FILLING);
    }

    public boolean canModifyIngredients() {
        return phase.canModifyIngredients();
    }
}
