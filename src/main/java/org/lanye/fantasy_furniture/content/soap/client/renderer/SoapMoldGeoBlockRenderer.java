package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapMoldDisplaySnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtils;

/**
 * 肥皂模具 BER：Geo 本体 + 盆内原料 Item；水面延迟至 {@link SoapMoldWaterOverlayPass}（C013 §5.4）。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapMoldGeoBlockRenderer implements BlockEntityRenderer<SoapMoldBlockEntity> {

    private final BodyRenderer bodyRenderer;

    public SoapMoldGeoBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.bodyRenderer = new BodyRenderer();
    }

    @Override
    public void render(
            SoapMoldBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        this.bodyRenderer.renderWithOverlays(
                blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private static final class BodyRenderer extends GeoBlockRenderer<SoapMoldBlockEntity> {

        private SoapMoldDisplaySnapshot snapshot = SoapMoldDisplaySnapshot.from(null);
        private final List<CapturedAnchor> capturedAnchors = new ArrayList<>();
        private final Set<String> capturedBonesThisFrame = new HashSet<>();

        BodyRenderer() {
            super(
                    new DefaultedBlockGeoModel<>(
                            ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "soap_mold")));
        }

        void renderWithOverlays(
                SoapMoldBlockEntity blockEntity,
                float partialTick,
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                int packedLight,
                int packedOverlay) {
            BlockPos blockPos = blockEntity.getBlockPos();
            this.snapshot = SoapMoldDisplaySnapshot.from(blockEntity);
            this.capturedAnchors.clear();
            this.capturedBonesThisFrame.clear();

            super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

            for (CapturedAnchor anchor : capturedAnchors) {
                if (anchor.kind() == AnchorKind.ITEM) {
                    anchor.drawItem(poseStack, bufferSource, packedLight, snapshot, partialTick);
                }
            }
            for (CapturedAnchor anchor : capturedAnchors) {
                if (anchor.kind() == AnchorKind.FLUID) {
                    anchor.enqueueWater(poseStack, packedLight, snapshot, partialTick, blockPos);
                }
            }
        }

        @Override
        public void applyRenderLayersForBone(
                PoseStack poseStack,
                SoapMoldBlockEntity blockEntity,
                GeoBone bone,
                RenderType renderType,
                MultiBufferSource bufferSource,
                VertexConsumer buffer,
                float partialTick,
                int packedLight,
                int packedOverlay) {
            super.applyRenderLayersForBone(
                    poseStack,
                    blockEntity,
                    bone,
                    renderType,
                    bufferSource,
                    buffer,
                    partialTick,
                    packedLight,
                    packedOverlay);

            ResourceLocation texture = getTextureLocation(blockEntity);
            if (!renderType.equals(RenderType.entityCutoutNoCull(texture))) {
                return;
            }

            SoapMoldDisplaySnapshot live = SoapMoldDisplaySnapshot.from(blockEntity);
            String boneName = bone.getName();
            if (!capturedBonesThisFrame.add(boneName)) {
                return;
            }

            if ("fluid_surface".equals(boneName) && live.showWater()) {
                capturedAnchors.add(CapturedAnchor.fluid(copyPoseAtPivot(poseStack, bone)));
            }
            for (SoapMoldDisplaySnapshot.DisplayItem entry : live.basinItems()) {
                if (entry.anchorBone().equals(boneName)) {
                    capturedAnchors.add(
                            CapturedAnchor.item(copyPoseAtPivot(poseStack, bone), entry.stack(), boneName));
                }
            }
        }

        private static PoseSnapshot copyPoseAtPivot(PoseStack poseStack, GeoBone bone) {
            poseStack.pushPose();
            RenderUtils.translateToPivotPoint(poseStack, bone);
            PoseSnapshot snapshot = copyPose(poseStack);
            poseStack.popPose();
            return snapshot;
        }

        private static PoseSnapshot copyPose(PoseStack poseStack) {
            PoseStack.Pose pose = poseStack.last();
            return new PoseSnapshot(new Matrix4f(pose.pose()), new Matrix3f(pose.normal()));
        }

        private record PoseSnapshot(Matrix4f pose, Matrix3f normal) {}

        private enum AnchorKind {
            ITEM,
            FLUID
        }

        private record CapturedAnchor(
                AnchorKind kind, PoseSnapshot transform, ItemStack stack, String anchorBone) {

            static CapturedAnchor item(PoseSnapshot transform, ItemStack stack, String anchorBone) {
                return new CapturedAnchor(AnchorKind.ITEM, transform, stack, anchorBone);
            }

            static CapturedAnchor fluid(PoseSnapshot transform) {
                return new CapturedAnchor(AnchorKind.FLUID, transform, ItemStack.EMPTY, "fluid_surface");
            }

            void drawItem(
                    PoseStack poseStack,
                    MultiBufferSource bufferSource,
                    int light,
                    SoapMoldDisplaySnapshot snapshot,
                    float partialTick) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                pose.pose().set(transform.pose());
                pose.normal().set(transform.normal());
                SoapMoldBasinItemRenderer.draw(
                        poseStack, bufferSource, light, stack, anchorBone, snapshot, partialTick);
                poseStack.popPose();
            }

            void enqueueWater(
                    PoseStack poseStack,
                    int light,
                    SoapMoldDisplaySnapshot snapshot,
                    float partialTick,
                    BlockPos blockPos) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                pose.pose().set(transform.pose());
                pose.normal().set(transform.normal());
                SoapMoldWaterOverlayPass.enqueue(
                        blockPos, new Matrix4f(pose.pose()), new Matrix3f(pose.normal()), light, snapshot, partialTick);
                poseStack.popPose();
            }
        }
    }
}
