package org.lanye.fantasy_furniture.content.soap;

/** 肥皂磨损档：0 默认、1 用过一次、2 用过两次。 */
public enum SoapBarWear {
    FULL(0, "soap_bar"),
    USED_ONCE(1, "soap_bar_used_once"),
    USED_TWICE(2, "soap_bar_used_twice");

    private final int index;
    private final String geoBasename;

    SoapBarWear(int index, String geoBasename) {
        this.index = index;
        this.geoBasename = geoBasename;
    }

    public int index() {
        return index;
    }

    public String geoBasename() {
        return geoBasename;
    }

    public static SoapBarWear fromIndex(int wear) {
        return switch (wear) {
            case 1 -> USED_ONCE;
            case 2 -> USED_TWICE;
            default -> FULL;
        };
    }

    public static int clamp(int wear) {
        if (wear <= 0) {
            return 0;
        }
        if (wear >= 2) {
            return 2;
        }
        return wear;
    }

    /** lang 键；默认档无额外磨损后缀。 */
    public static String wearTranslationKey(int wear) {
        return switch (clamp(wear)) {
            case 1 -> "soap.fantasy_furniture.wear.used_once";
            case 2 -> "soap.fantasy_furniture.wear.used_twice";
            default -> "soap.fantasy_furniture.wear.full";
        };
    }
}
