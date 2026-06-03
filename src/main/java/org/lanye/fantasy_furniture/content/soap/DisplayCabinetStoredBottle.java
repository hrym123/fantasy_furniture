package org.lanye.fantasy_furniture.content.soap;

/** 陈列柜内已放入的一瓶沐浴露或洗发露。 */
public record DisplayCabinetStoredBottle(DisplayCabinetBottleKind kind, int materialId) {

    public DisplayCabinetStoredBottle {
        materialId =
                switch (kind) {
                    case BODY_WASH ->
                            BodyWashMaterials.isValid(materialId)
                                    ? materialId
                                    : BodyWashMaterials.DEFAULT;
                    case SHAMPOO ->
                            ShampooMaterials.isValid(materialId)
                                    ? materialId
                                    : ShampooMaterials.DEFAULT;
                };
    }
}
