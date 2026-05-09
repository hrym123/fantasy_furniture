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
 * 普通玻璃窗：与 {@link org.lanye.reverie_core.geolib.client.GeolibAnimatedBlockRenderers#defaultGeoRendererProvider}
 * 同类方块（烤箱、橱柜等）一致，不使用床板系额外的 Y 轴 180° 补偿（该补偿仅适用于 Bedrock +Z 床体与 MC 水平朝向的特例）。
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
                        String b = PlainGlassWindowShapes.geoBasename(
                                entity.getBlockState().getValue(PlainGlassWindowBlock.SHAPE));
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
