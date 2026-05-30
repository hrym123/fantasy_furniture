package org.lanye.fantasy_furniture.content.soap;

/** 沐浴露颜料档：材质 id {@code 1}–{@link #COUNT}，贴图 {@code body_wash_{id}.png}。 */
public final class BodyWashMaterials {

    public static final int COUNT = SoapBarMaterials.COUNT;
    public static final int DEFAULT = SoapBarAppearance.DEFAULT_MATERIAL;

    private BodyWashMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    public static String colorTranslationKey(int materialId) {
        return SoapBarMaterials.colorTranslationKey(materialId);
    }
}
