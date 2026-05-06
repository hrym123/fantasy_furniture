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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6PillowPalette;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 床板 6：床体 Geo + 可选床单 Geo + 可选被套 Geo + 可选大号枕头 Geo + 可选中号枕头 Geo（须先有床单）。
 *
 * <p>中号枕头 Geo 选择（与大号可叠放；渲染顺序：大号后、中号最后）：
 *
 * <ul>
 *   <li>仅 1 个中号、且无大号 → {@code bed_plate6_pillow_medium_solo}（单人摆放）
 *   <li>仅 1 个中号、且有大号 → {@code bed_plate6_pillow_medium_pair_front}（与前排一致）
 *   <li>2 个中号 → {@code pair_rear}（先放槽）+ {@code pair_front}（后放槽）
 * </ul>
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

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> pillowLargeStriped =
            pillowLargeRenderer(BedPlate6LargePillowStyles.resourceSlug(1));
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> pillowLargePlain =
            pillowLargeRenderer(BedPlate6LargePillowStyles.resourceSlug(2));
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> pillowLargePlaid =
            pillowLargeRenderer(BedPlate6LargePillowStyles.resourceSlug(3));

    private final GeoBlockRenderer<BedPlateBaseBlockEntity> pillowMediumSolo =
            pillowMediumRenderer("solo", PillowMediumTextureMode.SOLO);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> pillowMediumPairRear =
            pillowMediumRenderer("pair_rear", PillowMediumTextureMode.PAIR_REAR);
    private final GeoBlockRenderer<BedPlateBaseBlockEntity> pillowMediumPairFront =
            pillowMediumRenderer("pair_front", PillowMediumTextureMode.PAIR_FRONT);

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
            if (b6.hasLargePillow()) {
                switch (b6.getLargePillowStyleId()) {
                    case 1 -> pillowLargeStriped.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                    case 2 -> pillowLargePlain.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                    case 3 -> pillowLargePlaid.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                    default -> {
                        /* unreachable when hasLargePillow */
                    }
                }
            }
            renderMediumPillows(b6, blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void renderMediumPillows(
            BedPlate6BlockEntity b6,
            BedPlateBaseBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int n = b6.getMediumPillowCount();
        if (n == 0) {
            return;
        }
        boolean large = b6.hasLargePillow();
        if (n == 2) {
            pillowMediumPairRear.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            pillowMediumPairFront.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        } else if (large) {
            pillowMediumPairFront.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        } else {
            pillowMediumSolo.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private enum PillowMediumTextureMode {
        SOLO,
        PAIR_REAR,
        PAIR_FRONT
    }

    private static GeoBlockRenderer<BedPlateBaseBlockEntity> pillowMediumRenderer(
            String layoutSlug, PillowMediumTextureMode mode) {
        ResourceLocation geo = ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/bed_plate6_pillow_medium_" + layoutSlug + ".geo.json");
        ResourceLocation anim = ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID,
                "animations/block/bed_plate6_pillow_medium_" + layoutSlug + ".animation.json");
        return new GeoBlockRenderer<BedPlateBaseBlockEntity>(
                new GeoModel<BedPlateBaseBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                        return geo;
                    }

                    @Override
                    public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                        int m = 1;
                        if (entity instanceof BedPlate6BlockEntity b6) {
                            m = switch (mode) {
                                case SOLO, PAIR_REAR -> b6.getMediumPillowMatFirst();
                                case PAIR_FRONT -> b6.getMediumPillowCount() == 2
                                        ? b6.getMediumPillowMatSecond()
                                        : b6.getMediumPillowMatFirst();
                            };
                            if (!BedPlate6MediumPillowMaterials.isValid(m)) {
                                m = 1;
                            }
                        }
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID, "textures/block/bed_plate6_pillow_medium_" + m + ".png");
                    }

                    @Override
                    public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                        return anim;
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
    }

    private static GeoBlockRenderer<BedPlateBaseBlockEntity> pillowLargeRenderer(String styleSlug) {
        ResourceLocation geo = ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/bed_plate6_pillow_large_" + styleSlug + ".geo.json");
        ResourceLocation anim = ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/bed_plate6_pillow_large_" + styleSlug + ".animation.json");
        return new GeoBlockRenderer<BedPlateBaseBlockEntity>(
                new GeoModel<BedPlateBaseBlockEntity>() {
                    @Override
                    public ResourceLocation getModelResource(BedPlateBaseBlockEntity entity) {
                        return geo;
                    }

                    @Override
                    public ResourceLocation getTextureResource(BedPlateBaseBlockEntity entity) {
                        int m = 1;
                        if (entity instanceof BedPlate6BlockEntity b6) {
                            m = b6.getLargePillowMaterialId();
                            if (!BedPlate6DuvetMaterials.isValid(m)) {
                                m = 1;
                            }
                        }
                        return ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "textures/block/bed_plate6_pillow_large_"
                                        + styleSlug
                                        + "_"
                                        + BedPlate6PillowPalette.colorSlug(m)
                                        + ".png");
                    }

                    @Override
                    public ResourceLocation getAnimationResource(BedPlateBaseBlockEntity entity) {
                        return anim;
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
    }
}
