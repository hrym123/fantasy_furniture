package org.lanye.fantasy_furniture.content.furniture.decor.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.ComputerBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.ComputerBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/** 电脑方块 geo：开合换模型，材质档换贴图（逻辑内联，避免拆类热重载丢 Class）。 */
public final class ComputerGeoModel extends GeoModel<ComputerBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ComputerBlockEntity animatable) {
        var state = animatable.getBlockState();
        boolean open = state.hasProperty(ComputerBlock.OPEN) && state.getValue(ComputerBlock.OPEN);
        return ComputerMaterials.geoLocation(animatable.closedAssetId(), open);
    }

    @Override
    public ResourceLocation getTextureResource(ComputerBlockEntity animatable) {
        var state = animatable.getBlockState();
        int material = state.hasProperty(ComputerBlock.MATERIAL)
                ? state.getValue(ComputerBlock.MATERIAL)
                : ComputerMaterials.DEFAULT;
        return ComputerMaterials.textureLocation(animatable.closedAssetId(), material);
    }

    @Override
    public ResourceLocation getAnimationResource(ComputerBlockEntity animatable) {
        return ComputerMaterials.animationLocation();
    }
}
