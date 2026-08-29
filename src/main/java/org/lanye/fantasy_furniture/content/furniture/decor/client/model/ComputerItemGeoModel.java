package org.lanye.fantasy_furniture.content.furniture.decor.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.client.ComputerItemRenderer;
import org.lanye.fantasy_furniture.content.furniture.decor.item.ComputerBlockItem;
import software.bernie.geckolib.model.GeoModel;

/** 物品栏 / 手持：关闭态 geo + 材质档贴图。 */
public final class ComputerItemGeoModel extends GeoModel<ComputerBlockItem> {

    private ComputerAppearance appearance() {
        ComputerAppearance a = ComputerItemRenderer.currentAppearance();
        return a != null ? a : ComputerAppearance.defaults();
    }

    private String closedAssetId() {
        String id = ComputerItemRenderer.currentClosedAssetId();
        return id != null ? id : "computer_1";
    }

    @Override
    public ResourceLocation getModelResource(ComputerBlockItem animatable) {
        return ComputerMaterials.geoLocation(closedAssetId(), false);
    }

    @Override
    public ResourceLocation getTextureResource(ComputerBlockItem animatable) {
        return ComputerMaterials.textureLocation(closedAssetId(), appearance().materialId());
    }

    @Override
    public ResourceLocation getAnimationResource(ComputerBlockItem animatable) {
        return ComputerMaterials.animationLocation();
    }
}
