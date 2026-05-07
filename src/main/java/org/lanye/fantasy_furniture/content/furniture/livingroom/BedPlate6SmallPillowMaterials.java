package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 床板 6 小号枕头六种外观（id 与 {@link BedPlate6PillowPalette} 床单色 1..6 一致；贴图 {@code bed_plate6_pillow_small_{id}.png}）。
 */
public final class BedPlate6SmallPillowMaterials {

    public static final int COUNT = 6;

    private BedPlate6SmallPillowMaterials() {}

    public static boolean isValid(int id) {
        return id >= 1 && id <= COUNT;
    }
}
