package org.lanye.fantasy_furniture.content.furniture.livingroom;

/** 共用床单材质数量与编号（1..{@link #COUNT}），与物品/各床型叠层贴图后缀一致。 */
public final class BedPlate6DuvetMaterials {

    public static final int COUNT = 8;

    /** 奶油色（展示名亦称米色）；物品材质源 {@code 白色.png}。 */
    public static final int CREAM = 7;

    /** 棕色；物品材质源 {@code 棕色.png}。 */
    public static final int BROWN = 8;

    private BedPlate6DuvetMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    /** 各床板世界叠层：八色均已补齐（见 U018）。 */
    public static boolean isSupportedOnBedPlate1(int materialId) {
        return isValid(materialId);
    }

    public static boolean isSupportedOnBedPlate3(int materialId) {
        return isValid(materialId);
    }

    public static boolean isSupportedOnBedPlate4(int materialId) {
        return isValid(materialId);
    }
}
