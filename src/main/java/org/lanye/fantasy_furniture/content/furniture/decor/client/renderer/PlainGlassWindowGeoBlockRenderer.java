package org.lanye.fantasy_furniture.content.furniture.decor.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowSharedTextures;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 普通玻璃窗：仅绑定 geo / 纹理 / 静态动画资源；姿态与几何完全以资源文件为准，
 * 本类<strong>不</strong>覆盖 {@link GeoBlockRenderer} 的渲染或旋转逻辑。
 */
@OnlyIn(Dist.CLIENT)
public final class PlainGlassWindowGeoBlockRenderer extends GeoBlockRenderer<PlainGlassWindowBlockEntity> {

    /** 与 {@link PlainGlassWindowBlockEntity} 的 {@code PlayState.STOP} 一致；多造型共用，避免每 geo 一份空动画 JSON。 */
    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public PlainGlassWindowGeoBlockRenderer() {
        super(
                new GeoModel<PlainGlassWindowBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(PlainGlassWindowBlockEntity entity) {
                        int shape = entity.getBlockState().getValue(PlainGlassWindowBlock.SHAPE);
                        String b = PlainGlassWindowShapes.geoBasename(shape);
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID, "geo/block/" + b + ".geo.json");
                    }

                    @Override
                    public ResourceLocation getTextureResource(PlainGlassWindowBlockEntity entity) {
                        int mat = PlainGlassWindowBlock.materialIndex(entity.getBlockState());
                        return PlainGlassWindowSharedTextures.textureLocationForStem(
                                FantasyFurniture.MODID, PlainGlassWindowMaterials.itemPreviewStem(mat));
                    }

                    @Override
                    public ResourceLocation getAnimationResource(PlainGlassWindowBlockEntity entity) {
                        return STATIC_ANIMATION;
                    }
                });
    }
}
