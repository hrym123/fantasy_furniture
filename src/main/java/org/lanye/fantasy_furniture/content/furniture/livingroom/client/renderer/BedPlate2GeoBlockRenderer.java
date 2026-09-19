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
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import org.lanye.reverie_core.geolib.client.GeoRenderTier;
import org.lanye.reverie_core.geolib.client.ReverieGeoBlockRenderer;

/**
 * 床板2型：素床体 + 分色枕头叠层 + 可选床单/被套。
 * 拼装 Geo 的贴图只有浅丁香紫，且模型里带被套，不再用来换床体。
 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate2GeoBlockRenderer implements BlockEntityRenderer<BedPlateBaseBlockEntity> {

    private static final ResourceLocation BODY_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate2.geo.json");
    private static final ResourceLocation BODY_TEX = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "textures/block/bed_plate2.png");
    private static final ResourceLocation BODY_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate2.animation.json");
    private static final ResourceLocation DUVET_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate2_duvet.geo.json");
    private static final ResourceLocation DUVET_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate2_duvet.animation.json");
    private static final ResourceLocation COVER_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate2_duvet_cover.geo.json");
    private static final ResourceLocation COVER_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate2_duvet_cover.animation.json");

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> bodyRenderer =
            footOnlyRenderer(ignored -> BODY_GEO, ignored -> BODY_TEX, BODY_ANIM);
    private final BedPlateComboPillowRenderer pillows = new BedPlateComboPillowRenderer(2);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> duvetRenderer =
            footOnlyRenderer(ignored -> DUVET_GEO, this::duvetTexture, DUVET_ANIM);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> coverRenderer =
            footOnlyRenderer(ignored -> COVER_GEO, this::coverTexture, COVER_ANIM);

    @Override
    public void render(
            BedPlateBaseBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        if (!(blockEntity instanceof BedPlate2BlockEntity plate2)) {
            return;
        }
        if (blockEntity.getBlockState().getValue(BedBlock.PART) != BedPart.FOOT) {
            return;
        }
        bodyRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        if (plate2.hasDuvet()) {
            duvetRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (plate2.hasCover()) {
            coverRenderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (plate2.hasLargePillowSlot(1)
                || plate2.hasLargePillowSlot(2)
                || plate2.hasMediumPillow()
                || plate2.hasSmallPillow()) {
            pillows.render(
                    blockEntity,
                    plate2.pillowSlots(),
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay);
        }
    }

    private ResourceLocation duvetTexture(BedPlateBaseBlockEntity entity) {
        int m = 1;
        if (entity instanceof BedPlate2BlockEntity plate2 && BedPlate6DuvetMaterials.isValid(plate2.getDuvetMaterialId())) {
            m = plate2.getDuvetMaterialId();
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate2_duvet_" + m + ".png");
    }

    private ResourceLocation coverTexture(BedPlateBaseBlockEntity entity) {
        int m = 1;
        if (entity instanceof BedPlate2BlockEntity plate2
                && BedPlate6DuvetCoverMaterials.isValid(plate2.getCoverMaterialId())) {
            m = plate2.getCoverMaterialId();
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/bed_plate2_duvet_cover_" + m + ".png");
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
