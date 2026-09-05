package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 共用被套材质编号（{@code 1..COUNT}），与 {@code bed_plate6_duvet_cover_{id}} /
 * 各床型叠层贴图后缀一致。
 *
 * <p>序对齐 Blockbench 英文贴图 stem：{@code purple → blue → green → pink → yellow → red}
 *（展示名 / 物品材质短名：紫 / 蓝 / 绿 / 粉 / 黄 / 红）。
 */
public final class BedPlate6DuvetCoverMaterials {

    public static final int COUNT = 6;

    private BedPlate6DuvetCoverMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    /** 床板1 叠层：六档均可。 */
    public static boolean isSupportedOnBedPlate1(int materialId) {
        return isValid(materialId);
    }

    /** 床板3 叠层：六档均可。 */
    public static boolean isSupportedOnBedPlate3(int materialId) {
        return isValid(materialId);
    }

    /** 床板4 叠层：六档均可。 */
    public static boolean isSupportedOnBedPlate4(int materialId) {
        return isValid(materialId);
    }
}
