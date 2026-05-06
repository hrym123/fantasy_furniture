package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 床板 6 中号枕头：六种外观材质（id 与 {@link BedPlate6PillowPalette} 床单色 1..6 一致：奶油、蔷薇、黄油、薄荷、丹宁、丁香；
 * 贴图 {@code bed_plate6_pillow_medium_{id}.png}）。
 */
public final class BedPlate6MediumPillowMaterials {

    public static final int COUNT = 6;

    private BedPlate6MediumPillowMaterials() {}

    /** {@code true} 当且仅当 {@code id} 在 {@code 1..COUNT}。 */
    public static boolean isValid(int id) {
        return id >= 1 && id <= COUNT;
    }
}
