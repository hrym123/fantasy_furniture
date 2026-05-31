package org.lanye.fantasy_furniture.content.soap;

/** 乳霜颜料档：材质 id {@code 1}–{@link #COUNT}，贴图 {@code body_cream_{id}.png}。 */
public final class BodyCreamMaterials {

    public static final int COUNT = SoapBarMaterials.COUNT;
    public static final int DEFAULT = SoapBarAppearance.DEFAULT_MATERIAL;

    /**
     * {@code 乳霜_默认.bbmodel} 贴图槽：green→1、yellow→2、blue→3、red→4、purple→5、pink→6；
     * 展示名仍用肥皂系六色 lang，按罐身实际颜色映射。
     */
    private static final String[] COLOR_TRANSLATION_KEYS = {
        "",
        "soap.fantasy_furniture.color.2",
        "soap.fantasy_furniture.color.5",
        "soap.fantasy_furniture.color.1",
        "soap.fantasy_furniture.color.6",
        "soap.fantasy_furniture.color.3",
        "soap.fantasy_furniture.color.4",
    };

    private BodyCreamMaterials() {}

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
