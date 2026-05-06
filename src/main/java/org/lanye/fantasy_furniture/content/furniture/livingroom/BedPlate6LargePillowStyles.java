package org.lanye.fantasy_furniture.content.furniture.livingroom;

/**
 * 床板 6 大号枕头：存储仍用 {@code 1..}{@link #COUNT}（方块实体 NBT 与旧存档兼容），资源路径用英文
 * {@link #resourceSlug(int)}（{@code striped} / {@code plain} / {@code plaid}）。
 *
 * <p>编号与旧「款式 1..3」一致：1=条纹，2=纯色，3=格子。
 */
public final class BedPlate6LargePillowStyles {

    public static final int COUNT = 3;

    /** 索引即存储 id；0 占位。 */
    private static final String[] RESOURCE_SLUG_BY_ID = {"", "striped", "plain", "plaid"};

    private BedPlate6LargePillowStyles() {}

    public static boolean isValid(int styleId) {
        return styleId >= 1 && styleId <= COUNT;
    }

    /** {@code bed_plate6_pillow_large_{slug}_{color}} 等路径用。 */
    public static String resourceSlug(int styleId) {
        if (!isValid(styleId)) {
            throw new IllegalArgumentException("styleId out of range: " + styleId);
        }
        return RESOURCE_SLUG_BY_ID[styleId];
    }
}
