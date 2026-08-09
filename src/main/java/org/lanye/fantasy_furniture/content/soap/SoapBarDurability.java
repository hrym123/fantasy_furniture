package org.lanye.fantasy_furniture.content.soap;

/**
 * 肥皂耐久度（原「磨损」）：剩余耐久 {@code 3}/{@code 2}/{@code 1}，决定 geo / 碰撞体积。
 *
 * <p>入水每满 1 分钟消耗 1 点；从 1 再耗尽则消失。旧 NBT {@code SoapWear}（0/1/2）= {@code MAX - wear}。
 */
public enum SoapBarDurability {
    FULL(3, "soap_bar"),
    MID(2, "soap_bar_used_once"),
    LOW(1, "soap_bar_used_twice");

    public static final int MAX = 3;
    public static final int MIN = 1;

    private final int remaining;
    private final String geoBasename;

    SoapBarDurability(int remaining, String geoBasename) {
        this.remaining = remaining;
        this.geoBasename = geoBasename;
    }

    public int remaining() {
        return remaining;
    }

    public String geoBasename() {
        return geoBasename;
    }

    public static SoapBarDurability fromRemaining(int remaining) {
        return switch (clamp(remaining)) {
            case 2 -> MID;
            case 1 -> LOW;
            default -> FULL;
        };
    }

    public static int clamp(int remaining) {
        if (remaining <= MIN) {
            return MIN;
        }
        if (remaining >= MAX) {
            return MAX;
        }
        return remaining;
    }

    /** 旧磨损档 0/1/2 → 剩余耐久 3/2/1。 */
    public static int fromLegacyWear(int wear) {
        if (wear <= 0) {
            return MAX;
        }
        if (wear >= 2) {
            return MIN;
        }
        return MAX - wear;
    }

    /** lang 键；满耐久无额外后缀时可不用。 */
    public static String translationKey(int remaining) {
        return switch (clamp(remaining)) {
            case 2 -> "soap.fantasy_furniture.durability.2";
            case 1 -> "soap.fantasy_furniture.durability.1";
            default -> "soap.fantasy_furniture.durability.3";
        };
    }
}
