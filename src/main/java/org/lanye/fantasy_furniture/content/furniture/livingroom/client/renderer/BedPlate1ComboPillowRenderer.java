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
 * 床板1型枕头：几何来自组合 bbmodel 拆出的 geo，不在这里平移。
 * 大号两槽：竖放 S / 平放 P；中号平放 P / 竖放 S；小号 X4–X7。
 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate1ComboPillowRenderer {

    private static final ResourceLocation ANIM =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/bed_plate1.animation.json");

    private String geoName = "bed_plate1_pillow_large_s1";
    private String texturePath = "textures/block/bed_plate6_pillow_large_plain_cream.png";

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> renderer =
            new GeoBlockRenderer<>(
                    new GeoModel<>() {
                        @Override
                        public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                            return ResourceLocation.fromNamespaceAndPath(
                                    FantasyFurniture.MODID, "geo/block/" + geoName + ".geo.json");
                        }

                        @Override
                        public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                            return ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, texturePath);
                        }

                        @Override
                        public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                            return ANIM;
                        }
                    });

    public void render(
            BedPlateBaseBlockEntity blockEntity,
            BedPlateSheetPillowSlots slots,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (slots.hasLargeSlot(1)) {
            drawLarge(
                    blockEntity,
                    slots,
                    1,
                    (slots.largeFlat(1) ? "p" : "s") + "1",
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay);
        }
        if (slots.hasLargeSlot(2)) {
            drawLarge(
                    blockEntity,
                    slots,
                    2,
                    (slots.largeFlat(2) ? "p" : "s") + "2",
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay);
        }
        if (slots.hasMedium()) {
            int side = slots.mediumSide();
            this.geoName = "bed_plate1_pillow_medium_" + (slots.mediumStanding() ? "s" : "p") + side;
            int mat = slots.mediumMat();
            if (!BedPlate6MediumPillowMaterials.isValid(mat)) {
                mat = 1;
            }
            this.texturePath = "textures/block/bed_plate6_pillow_medium_" + mat + ".png";
            renderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (slots.hasSmall()) {
            this.geoName = "bed_plate1_pillow_small_x" + slots.smallPlace();
            int mat = slots.smallMat();
            if (!BedPlate6SmallPillowMaterials.isValid(mat)) {
                mat = 1;
            }
            this.texturePath = "textures/block/bed_plate6_pillow_small_" + mat + ".png";
            renderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void drawLarge(
            BedPlateBaseBlockEntity blockEntity,
            BedPlateSheetPillowSlots slots,
            int side,
            String geoSuffix,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int style = slots.largeStyleOnSide(side);
        int mat = slots.largeMaterialOnSide(side);
        if (!BedPlate6LargePillowStyles.isValid(style) || !BedPlate6DuvetMaterials.isValid(mat)) {
            return;
        }
        this.geoName = "bed_plate1_pillow_large_" + geoSuffix;
        this.texturePath =
                "textures/block/bed_plate6_pillow_large_"
                        + BedPlate6LargePillowStyles.resourceSlug(style)
                        + "_"
                        + BedPlate6PillowPalette.colorSlug(mat)
                        + ".png";
        renderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
