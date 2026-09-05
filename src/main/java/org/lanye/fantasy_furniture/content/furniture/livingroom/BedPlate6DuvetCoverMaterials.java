package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 床板 6 被套材质编号（{@code 1..COUNT}），与 {@code bed_plate6_duvet_cover_{id}.png} 一致。
 * 被套仅六种（与床单色 1..6 对应）；床单仍保留七种含可可色第 7 号。
 */
public final class BedPlate6DuvetCoverMaterials {

    public static final int COUNT = 6;

    /** 奶油色；床板1 组合源暂无对应贴图，禁止铺上。 */
    public static final int CREAM = 2;

    private BedPlate6DuvetCoverMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    /** 床板1 叠层可用档：缺奶油 {@link #CREAM} 专用贴图。 */
    public static boolean isSupportedOnBedPlate1(int materialId) {
        return isValid(materialId) && materialId != CREAM;
    }
}
