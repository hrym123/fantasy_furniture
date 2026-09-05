package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 床板1型：2×2 仅床尾右绘床体；可选叠床单 / 被套。
 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate1GeoBlockRenderer implements BlockEntityRenderer<BedPlateBaseBlockEntity> {

    private static final ResourceLocation BODY_ANIM =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/bed_plate1.animation.json");
    private static final ResourceLocation DUVET_GEO =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/bed_plate1_duvet.geo.json");
    private static final ResourceLocation DUVET_ANIM =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/bed_plate1_duvet.animation.json");
    private static final ResourceLocation COVER_GEO =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "geo/block/bed_plate1_duvet_cover.geo.json");
    private static final ResourceLocation COVER_ANIM =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/bed_plate1_duvet_cover.animation.json");

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> bodyRenderer =
            anchorOnlyRenderer(this::bodyGeo, this::bodyTexture, BODY_ANIM);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> duvetRenderer =
            anchorOnlyRenderer(ignored -> DUVET_GEO, this::duvetTexture, DUVET_ANIM);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> coverRenderer =
            anchorOnlyRenderer(ignored -> COVER_GEO, this::coverTexture, COVER_ANIM);

    @Override
    public void render(
            BedPlateBaseBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (!(blockEntity instanceof BedPlate1BlockEntity)) {
            return;
        }
        if (!BedPlate1Block.isRenderAnchor(blockEntity.getBlockState())) {
            return;
        }
        bodyRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        BedPlate1BlockEntity decor =
                BedPlate1Block.decorEntity(
                        blockEntity.getLevel(),
                        blockEntity.getBlockState(),
                        blockEntity.getBlockPos());
        if (decor != null
                && BedPlate6DuvetMaterials.isSupportedOnBedPlate1(decor.getDuvetMaterialId())) {
            duvetRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (decor != null
                && BedPlate6DuvetCoverMaterials.isSupportedOnBedPlate1(decor.getCoverMaterialId())) {
            coverRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private ResourceLocation bodyGeo(BedPlateBaseBlockEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/bed_plate1.geo.json");
    }

    private ResourceLocation bodyTexture(BedPlateBaseBlockEntity entity) {
        if (entity instanceof BedPlate1BlockEntity plate1) {
            return plate1.getTextureLocation();
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate1_beige.png");
    }

    private ResourceLocation duvetTexture(BedPlateBaseBlockEntity entity) {
        int m = 1;
        if (entity.getLevel() != null) {
            BedPlate1BlockEntity decor =
                    BedPlate1Block.decorEntity(
                            entity.getLevel(), entity.getBlockState(), entity.getBlockPos());
            if (decor != null
                    && BedPlate6DuvetMaterials.isSupportedOnBedPlate1(decor.getDuvetMaterialId())) {
                m = decor.getDuvetMaterialId();
            }
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate1_duvet_" + m + ".png");
    }

    private ResourceLocation coverTexture(BedPlateBaseBlockEntity entity) {
        int m = 1;
        if (entity.getLevel() != null) {
            BedPlate1BlockEntity decor =
                    BedPlate1Block.decorEntity(
                            entity.getLevel(), entity.getBlockState(), entity.getBlockPos());
            if (decor != null
                    && BedPlate6DuvetCoverMaterials.isSupportedOnBedPlate1(
                            decor.getCoverMaterialId())) {
                m = decor.getCoverMaterialId();
            }
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate1_duvet_cover_" + m + ".png");
    }

    private static GeoBlockRenderer<BedPlateBaseBlockEntity> anchorOnlyRenderer(
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
                if (!BedPlate1Block.isRenderAnchor(animatable.getBlockState())) {
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
