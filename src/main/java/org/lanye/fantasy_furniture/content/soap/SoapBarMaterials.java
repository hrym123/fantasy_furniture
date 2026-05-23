package org.lanye.fantasy_furniture.content.soap;

/** 肥皂颜料档：材质 id {@code 1}–{@link #COUNT}，与贴图 {@code soap_bar_{id}} 一致。 */
public final class SoapBarMaterials {

    public static final int COUNT = 6;

    private SoapBarMaterials() {}

    public static boolean isValid(int materialId) {
        return materialId >= 1 && materialId <= COUNT;
    }

    /** lang 键：{@code soap.fantasy_furniture.color.{id}} */
    public static String colorTranslationKey(int materialId) {
        return "soap.fantasy_furniture.color." + materialId;
    }
}
