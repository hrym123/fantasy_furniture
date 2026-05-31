package org.lanye.fantasy_furniture.content.soap;

/** 洗发露颜料档：材质 id {@code 1}–{@link #COUNT}，贴图 {@code shampoo_{id}.png}。 */
public final class ShampooMaterials {

    public static final int COUNT = SoapBarMaterials.COUNT;
    public static final int DEFAULT = SoapBarAppearance.DEFAULT_MATERIAL;

    /**
     * {@code 洗发露_默认.bbmodel} 贴图槽：purple→1、blue→2、pink→3、green→4、yellow→5、red→6；
     * 展示名仍用肥皂系六色 lang，按瓶身实际颜色映射。
     */
    private static final String[] COLOR_TRANSLATION_KEYS = {
        "",
        "soap.fantasy_furniture.color.3",
        "soap.fantasy_furniture.color.1",
        "soap.fantasy_furniture.color.4",
        "soap.fantasy_furniture.color.2",
        "soap.fantasy_furniture.color.5",
        "soap.fantasy_furniture.color.6",
    };

    private ShampooMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    public static String colorTranslationKey(int materialId) {
        if (!isValid(materialId)) {
            throw new IllegalArgumentException("materialId out of range: " + materialId);
        }
        return COLOR_TRANSLATION_KEYS[materialId];
    }
}
