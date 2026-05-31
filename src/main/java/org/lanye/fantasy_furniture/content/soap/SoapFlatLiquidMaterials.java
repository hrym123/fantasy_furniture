package org.lanye.fantasy_furniture.content.soap;

/**
 * 单材质图液体原料六色（沐浴液 / 洗发液）：材质 id 与 {@code {stem}_ui_{id}.png}、moonstarfish
 * {@code 物品材质/} 文件名（粉→1、紫→2、红→3、绿→4、蓝→5、黄→6）一致；展示名与肥皂套系共用
 * {@link SoapBarMaterials} 六色 lang。
 */
public final class SoapFlatLiquidMaterials {

    public static final int COUNT = SoapBarMaterials.COUNT;

    /** 液体 id → 肥皂系颜料展示名（{@code soap.fantasy_furniture.color.*}）。 */
    private static final String[] COLOR_TRANSLATION_KEYS = {
        "",
        "soap.fantasy_furniture.color.4",
        "soap.fantasy_furniture.color.3",
        "soap.fantasy_furniture.color.6",
        "soap.fantasy_furniture.color.2",
        "soap.fantasy_furniture.color.1",
        "soap.fantasy_furniture.color.5",
    };

    private SoapFlatLiquidMaterials() {}

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
