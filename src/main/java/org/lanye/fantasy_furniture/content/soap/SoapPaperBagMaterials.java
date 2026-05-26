package org.lanye.fantasy_furniture.content.soap;

/** 包装袋材质档：{@code 1}–{@code 6} 可玩法；{@code 7} 彩色仅展示。 */
public final class SoapPaperBagMaterials {

    public static final int COUNT = 7;
    public static final int RAINBOW = 7;
    public static final int DEFAULT = SoapBarAppearance.DEFAULT_MATERIAL;

    private SoapPaperBagMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    /** 可套皂、可参与世界摞（不含彩色）。 */
    public static boolean isPlayable(int materialId) {
        return materialId >= 1 && materialId <= SoapBarMaterials.COUNT;
    }

    public static String colorTranslationKey(int materialId) {
        if (materialId == RAINBOW) {
            return "soap.fantasy_furniture.color.rainbow";
        }
        return SoapBarMaterials.colorTranslationKey(materialId);
    }
}
