package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6PillowPalette;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6SmallPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 已铺床单的床板上，把床板6型枕头 Geo 叠在床单之上。
 * 几何沿用现成枕头模型，不在这里加平移。
 */
@OnlyIn(Dist.CLIENT)
public final class BedPlatePillowLayerRenderer {

    private int largeStyle;
    private int largeMat;
    private int mediumMat;
    private int smallMat;

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> striped = large("striped");
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> plain = large("plain");
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> plaid = large("plaid");
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> mediumSolo = medium("solo");
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> mediumFront = medium("pair_front");
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> small =
            fixed(
                    "geo/block/bed_plate6_pillow_small_stack.geo.json",
                    "animations/block/bed_plate6_pillow_small_stack.animation.json",
                    () -> texture("textures/block/bed_plate6_pillow_small_" + validSmall() + ".png"));

    public void render(
            BedPlateBaseBlockEntity blockEntity,
            BedPlateSheetPillowSlots slots,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        render(
                blockEntity,
                slots.largeStyleId(),
                slots.largeMaterialId(),
                slots.mediumMat(),
                slots.smallMat(),
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay);
    }

    public void render(
            BedPlateBaseBlockEntity blockEntity,
            int largeStyleId,
            int largeMaterialId,
            int mediumMaterialId,
            int smallMaterialId,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        this.largeStyle = largeStyleId;
        this.largeMat = largeMaterialId;
        this.mediumMat = mediumMaterialId;
        this.smallMat = smallMaterialId;
        if (BedPlate6LargePillowStyles.isValid(largeStyleId) && BedPlate6DuvetMaterials.isValid(largeMaterialId)) {
            GeoBlockRenderer<BedPlateBaseBlockEntity> layer =
                    switch (largeStyleId) {
                        case 1 -> striped;
                        case 2 -> plain;
                        default -> plaid;
                    };
            layer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (BedPlate6MediumPillowMaterials.isValid(mediumMaterialId)) {
            GeoBlockRenderer<BedPlateBaseBlockEntity> layer =
                    BedPlate6LargePillowStyles.isValid(largeStyleId) ? mediumFront : mediumSolo;
            layer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (BedPlate6SmallPillowMaterials.isValid(smallMaterialId)) {
            small.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private GeoBlockRenderer<BedPlateBaseBlockEntity> large(String styleSlug) {
        return fixed(
                "geo/block/bed_plate6_pillow_large_" + styleSlug + ".geo.json",
                "animations/block/bed_plate6_pillow_large_" + styleSlug + ".animation.json",
                () -> texture(
                        "textures/block/bed_plate6_pillow_large_"
                                + styleSlug
                                + "_"
                                + BedPlate6PillowPalette.colorSlug(validLargeMat())
                                + ".png"));
    }

    private GeoBlockRenderer<BedPlateBaseBlockEntity> medium(String layout) {
        return fixed(
                "geo/block/bed_plate6_pillow_medium_" + layout + ".geo.json",
                "animations/block/bed_plate6_pillow_medium_" + layout + ".animation.json",
                () -> texture("textures/block/bed_plate6_pillow_medium_" + validMedium() + ".png"));
    }

    private int validLargeMat() {
        return BedPlate6DuvetMaterials.isValid(largeMat) ? largeMat : 1;
    }

    private int validMedium() {
        return BedPlate6MediumPillowMaterials.isValid(mediumMat) ? mediumMat : 1;
    }

    private int validSmall() {
        return BedPlate6SmallPillowMaterials.isValid(smallMat) ? smallMat : 1;
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, path);
    }

    private GeoBlockRenderer<BedPlateBaseBlockEntity> fixed(String geoPath, String animPath, Texture textureFn) {
        ResourceLocation geo = texture(geoPath);
        ResourceLocation anim = texture(animPath);
        return new GeoBlockRenderer<>(
                new GeoModel<>() {
                    @Override
                    public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                        return geo;
                    }

                    @Override
                    public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                        return textureFn.get();
                    }

                    @Override
                    public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                        return anim;
                    }
                });
    }

    @FunctionalInterface
    private interface Texture {
        ResourceLocation get();
    }
}
