package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 床板 6 大号枕头七种配色（与床单材质 id {@code 1..7} 对齐）的英文资源 slug。
 *
 * <p>MoonStarfish 工程中纯色 / 格子大号枕头仅含 6 张贴图时，第 7 色 {@code cocoa} 复用第 6 色贴图，与
 * {@code tools/extract_bed_plate6_pillow_large_textures_from_bbmodel.py} 一致（纯色/格子可可棕、条纹奶油色大号物品已移除；
 * 条纹可可仍用 {@code cocoa} slug）。
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
    };

    private BedPlate6PillowPalette() {}

    public static String colorSlug(int materialId) {
        if (!BedPlate6DuvetMaterials.isValid(materialId)) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        return COLOR_SLUGS[materialId];
    }
}
