package org.lanye.fantasy_furniture.content.furniture.cabinet.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetAppearance;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetMaterials;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.CabinetItemRenderer;
import org.lanye.fantasy_furniture.content.furniture.cabinet.item.CabinetBlockItem;
import software.bernie.geckolib.model.GeoModel;

/** 物品栏 / 手持：按 NBT 材质档换贴图。 */
public final class CabinetItemGeoModel extends GeoModel<CabinetBlockItem> {

    private CabinetKind kind() {
        CabinetKind k = CabinetItemRenderer.currentKind();
        return k != null ? k : CabinetKind.CABINET_1;
    }

    private CabinetAppearance appearance() {
        CabinetAppearance a = CabinetItemRenderer.currentAppearance();
        return a != null ? a : CabinetAppearance.defaults();
    }

    @Override
    public ResourceLocation getModelResource(CabinetBlockItem animatable) {
        return CabinetMaterials.itemGeoLocation(kind());
    }

    @Override
    public ResourceLocation getTextureResource(CabinetBlockItem animatable) {
        return CabinetMaterials.textureLocation(kind(), appearance().materialId());
    }

    @Override
    public ResourceLocation getAnimationResource(CabinetBlockItem animatable) {
        return CabinetMaterials.itemAnimationLocation(kind());
    }
}
