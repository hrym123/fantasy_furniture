package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 床板3型：床体 Geo + 可选床单 / 被套 / 分色枕头。 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate3GeoBlockRenderer implements BlockEntityRenderer<BedPlateBaseBlockEntity> {

    private static final ResourceLocation BODY_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate3.geo.json");
    private static final ResourceLocation BODY_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate3.animation.json");
    private static final ResourceLocation DUVET_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate3_duvet.geo.json");
    private static final ResourceLocation DUVET_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate3_duvet.animation.json");
    private static final ResourceLocation COVER_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate3_duvet_cover.geo.json");
    private static final ResourceLocation COVER_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate3_duvet_cover.animation.json");

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> bodyRenderer =
            footOnlyRenderer(ignored -> BODY_GEO, this::bodyTexture, BODY_ANIM);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> duvetRenderer =
            footOnlyRenderer(ignored -> DUVET_GEO, this::duvetTexture, DUVET_ANIM);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> coverRenderer =
            footOnlyRenderer(ignored -> COVER_GEO, this::coverTexture, COVER_ANIM);
    private final BedPlateComboPillowRenderer pillows = new BedPlateComboPillowRenderer(3);

    @Override
    public void render(
            BedPlateBaseBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (!(blockEntity instanceof BedPlate3BlockEntity plate3)) {
            return;
        }
        if (blockEntity.getBlockState().getValue(BedBlock.PART) != BedPart.FOOT) {
            return;
        }
        bodyRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        if (plate3.hasDuvet()
                && BedPlate6DuvetMaterials.isSupportedOnBedPlate3(plate3.getDuvetMaterialId())) {
            duvetRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (plate3.hasCover()
                && BedPlate6DuvetCoverMaterials.isSupportedOnBedPlate3(plate3.getCoverMaterialId())) {
            coverRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (plate3.sheetPillows().hasAny()) {
            pillows.render(
                    blockEntity,
                    plate3.sheetPillows(),
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay);
        }
    }

    private ResourceLocation bodyTexture(BedPlateBaseBlockEntity entity) {
        if (entity instanceof BedPlate3BlockEntity plate3) {
            return plate3.getTextureLocation();
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate3_beige.png");
    }

    private ResourceLocation duvetTexture(BedPlateBaseBlockEntity entity) {
        int m = 1;
        if (entity instanceof BedPlate3BlockEntity plate3
                && BedPlate6DuvetMaterials.isSupportedOnBedPlate3(plate3.getDuvetMaterialId())) {
            m = plate3.getDuvetMaterialId();
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate3_duvet_" + m + ".png");
    }

    private ResourceLocation coverTexture(BedPlateBaseBlockEntity entity) {
        int m = 1;
        if (entity instanceof BedPlate3BlockEntity plate3
                && BedPlate6DuvetCoverMaterials.isSupportedOnBedPlate3(plate3.getCoverMaterialId())) {
            m = plate3.getCoverMaterialId();
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate3_duvet_cover_" + m + ".png");
    }

    private static GeoBlockRenderer<BedPlateBaseBlockEntity> footOnlyRenderer(
            java.util.function.Function<BedPlateBaseBlockEntity, ResourceLocation> geoFn,
            java.util.function.Function<BedPlateBaseBlockEntity, ResourceLocation> textureFn,
            ResourceLocation anim) {
        return new ReverieGeoBlockRenderer<BedPlateBaseBlockEntity>(
                new GeoModel<BedPlateBaseBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                        return geoFn.apply(entity);
                    }

                    @Override
                    public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                        return textureFn.apply(entity);
                    }

                    @Override
                    public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                        return anim;
                    }
                },
                GeoRenderTier.STATIC) {
            @Override
            public void actuallyRender(
                    PoseStack poseStack,
                    BedPlateBaseBlockEntity animatable,
                    BakedGeoModel model,
                    RenderType renderType,
                    MultiBufferSource bufferSource,
                    VertexConsumer buffer,
                    boolean isReRender,
                    float partialTick,
                    int packedLight,
                    int packedOverlay,
                    float red,
                    float green,
                    float blue,
                    float alpha) {
                if (animatable.getBlockState().getValue(BedBlock.PART) != BedPart.FOOT) {
                    return;
                }
                super.actuallyRender(
                        poseStack,
                        animatable,
                        model,
                        renderType,
                        bufferSource,
                        buffer,
                        isReRender,
                        partialTick,
                        packedLight,
                        packedOverlay,
                        red,
                        green,
                        blue,
                        alpha);
            }
        };
    }
}
