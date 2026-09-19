package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateComboPillowLayout;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 床板2/3/4 枕头：几何来自该型号组合 bbmodel，贴图仍用共用枕头 PNG。
 * 不在这里加平移。
 */
@OnlyIn(Dist.CLIENT)
public final class BedPlateComboPillowRenderer {

    private final int plate;
    private final ResourceLocation anim;
    private String geoName;
    private String texturePath;

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
                            return anim;
                        }
                    });

    public BedPlateComboPillowRenderer(int plate) {
        this.plate = plate;
        this.anim =
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "animations/block/bed_plate" + plate + ".animation.json");
        this.geoName = "bed_plate" + plate + "_pillow_large_p1";
        this.texturePath = "textures/block/bed_plate6_pillow_large_plain_cream.png";
    }

    public void render(
            BedPlateBaseBlockEntity blockEntity,
            BedPlateSheetPillowSlots slots,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        List<String> suffixes = BedPlateComboPillowLayout.suffixes(plate, slots);
        for (String suffix : suffixes) {
            if (!bind(slots, suffix)) {
                continue;
            }
            renderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private boolean bind(BedPlateSheetPillowSlots slots, String suffix) {
        this.geoName = "bed_plate" + plate + "_pillow_" + suffix;
        if (suffix.startsWith("large_")) {
            int side = suffix.endsWith("2") ? 2 : 1;
            int style = slots.largeStyleOnSide(side);
            int mat = slots.largeMaterialOnSide(side);
            if (suffix.endsWith("p1")) {
                style = slots.largeStyleId();
                mat = slots.largeMaterialId();
            }
            if (!BedPlate6LargePillowStyles.isValid(style) || !BedPlate6DuvetMaterials.isValid(mat)) {
                return false;
            }
            this.texturePath =
                    "textures/block/bed_plate6_pillow_large_"
                            + BedPlate6LargePillowStyles.resourceSlug(style)
                            + "_"
                            + BedPlate6PillowPalette.colorSlug(mat)
                            + ".png";
            return true;
        }
        if (suffix.startsWith("medium_")) {
            int mat = slots.mediumMat();
            if (!BedPlate6MediumPillowMaterials.isValid(mat)) {
                return false;
            }
            this.texturePath = "textures/block/bed_plate6_pillow_medium_" + mat + ".png";
            return true;
        }
        int mat = slots.smallMat();
        if (!BedPlate6SmallPillowMaterials.isValid(mat)) {
            return false;
        }
        this.texturePath = "textures/block/bed_plate6_pillow_small_" + mat + ".png";
        return true;
    }
}
