package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 床板 6 大号枕头配色英文资源 slug（历史序，与床单展示色名<strong>不必</strong>一一同名）。
 *
 * <p>有效大号枕头材质仍为 {@code 1..7}；床单第 8 色棕色不参与大号枕头注册（见
 * {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem#isUnavailableLargeVariant}）。
 * 下表第 8 槽仅防越界。
 */
public final class BedPlate6PillowPalette {

    private static final String[] COLOR_SLUGS = {
        "", // 0 unused
        "cream",
        "rose",
        "butter",
        "mint",
        "denim",
        "lilac",
        "cocoa",
        "brown", // 对齐床单 BROWN=8；大号枕头不注册
    };

    private BedPlate6PillowPalette() {}

    public static String colorSlug(int materialId) {
        if (!BedPlate6DuvetMaterials.isValid(materialId)) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        return COLOR_SLUGS[materialId];
    }
}
