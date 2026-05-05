package org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 床板 6：床体 Geo + 可选床单 Geo + 可选被套 Geo（须先有床单；贴图按方块实体编号切换）。
 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate6GeoBlockRenderer implements BlockEntityRenderer<BedPlateBaseBlockEntity> {

    private static final ResourceLocation DUVET_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate6_duvet.geo.json");
    private static final ResourceLocation DUVET_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate6_duvet.animation.json");
    private static final ResourceLocation COVER_GEO = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "geo/block/bed_plate6_duvet_cover.geo.json");
    private static final ResourceLocation COVER_ANIM = ResourceLocation.fromNamespaceAndPath(
            FantasyFurniture.MODID, "animations/block/bed_plate6_duvet_cover.animation.json");

    private final BedPlateGeoBlockRenderer bed =
            new BedPlateGeoBlockRenderer(FantasyFurniture.MODID, "bed_plate6");

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> duvet =
            new GeoBlockRenderer<BedPlateBaseBlockEntity>(
                    new GeoModel<BedPlateBaseBlockEntity>() {
                        @Override
                        public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                            return DUVET_GEO;
                        }

                        @Override
                        public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                            int m = 1;
                            if (entity instanceof BedPlate6BlockEntity b6) {
                                m = b6.getDuvetMaterialId();
                                if (!BedPlate6DuvetMaterials.isValid(m)) {
                                    m = 1;
                                }
                            }
                            return ResourceLocation.fromNamespaceAndPath(
                                    FantasyFurniture.MODID, "textures/block/bed_plate6_duvet_" + m + ".png");
                        }

                        @Override
                        public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                            return DUVET_ANIM;
                        }
                    }) {
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

                @Override
                protected void rotateBlock(Direction facing, PoseStack poseStack) {
                    super.rotateBlock(facing, poseStack);
                    if (facing.getAxis() != Direction.Axis.Y) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    }
                }
            };

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> duvetCover =
            new GeoBlockRenderer<BedPlateBaseBlockEntity>(
                    new GeoModel<BedPlateBaseBlockEntity>() {
                        @Override
                        public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                            return COVER_GEO;
                        }

                        @Override
                        public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                            int m = 1;
                            if (entity instanceof BedPlate6BlockEntity b6) {
                                m = b6.getCoverMaterialId();
                                if (!BedPlate6DuvetMaterials.isValid(m)) {
                                    m = 1;
                                }
                            }
                            return ResourceLocation.fromNamespaceAndPath(
                                    FantasyFurniture.MODID, "textures/block/bed_plate6_duvet_cover_" + m + ".png");
                        }

                        @Override
                        public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                            return COVER_ANIM;
                        }
                    }) {
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

                @Override
                protected void rotateBlock(Direction facing, PoseStack poseStack) {
                    super.rotateBlock(facing, poseStack);
                    if (facing.getAxis() != Direction.Axis.Y) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    }
                }
            };

    @Override
    public void render(
            BedPlateBaseBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        bed.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        if (blockEntity instanceof BedPlate6BlockEntity b6 && b6.hasDuvet()) {
            duvet.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            if (b6.hasCover()) {
                duvetCover.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }
    }
}
