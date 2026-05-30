package org.lanye.fantasy_furniture.content.soap;

/** 包装盒盒色：材质 id {@code 1}–{@link #COUNT}，贴图 {@code soap_paper_box_{id}.png}。 */
public final class SoapPaperBoxMaterials {

    public static final int COUNT = SoapBarMaterials.COUNT;
    public static final int DEFAULT = SoapBarAppearance.DEFAULT_MATERIAL;

    private SoapPaperBoxMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    public static String colorTranslationKey(int materialId) {
        return SoapBarMaterials.colorTranslationKey(materialId);
    }
}
