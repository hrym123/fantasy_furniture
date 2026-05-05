package org.lanye.fantasy_furniture.content.furniture.livingroom;

/** 床板 6 床单材质数量与编号（1..{@link #COUNT}），与物品/贴图后缀一致。 */
public final class BedPlate6DuvetMaterials {

    public static final int COUNT = 7;

    private BedPlate6DuvetMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }
}
