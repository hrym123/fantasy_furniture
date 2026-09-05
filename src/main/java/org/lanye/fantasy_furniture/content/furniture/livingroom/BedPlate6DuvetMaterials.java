package org.lanye.fantasy_furniture.content.furniture.livingroom;

/** 床板 6 床单材质数量与编号（1..{@link #COUNT}），与物品/贴图后缀一致。 */
public final class BedPlate6DuvetMaterials {

    public static final int COUNT = 7;

    /** 奶油色（展示名亦称米色）；床板1 组合源暂无对应贴图，禁止铺上。 */
    public static final int CREAM = 7;

    private BedPlate6DuvetMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    /**
     * 床板1 叠层可用档：组合 bbmodel 仅有 _1…_6；{@link #CREAM} 待 moonstarfish 补图（Opt-021 / U017）。
     */
    public static boolean isSupportedOnBedPlate1(int materialId) {
        return isValid(materialId) && materialId != CREAM;
    }

    /** 床板3：组合源仅 _1…_6；奶油色禁止。 */
    public static boolean isSupportedOnBedPlate3(int materialId) {
        return isValid(materialId) && materialId != CREAM;
    }

    /**
     * 床板4：组合源暂无专用床单贴图，暂借板3 六色；奶油色禁止。
     * 补齐 {@code *_bed_sheet_4} 后应重导替换。
     */
    public static boolean isSupportedOnBedPlate4(int materialId) {
        return isValid(materialId) && materialId != CREAM;
    }
}
