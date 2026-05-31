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
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapMoldDisplaySnapshot;
import org.lanye.fantasy_furniture.content.soap.client.model.SoapMoldBlockGeoModel;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import org.lanye.reverie_core.client.renderer.container.ContainerFluidSurfacePass;
import org.lanye.reverie_core.client.renderer.container.ContainerFluidSurfaceSpec;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtils;

/**
 * 肥皂模具 BER：Geo 本体 + 盆内原料 Item；水面延迟至 {@link ContainerFluidSurfacePass}（C013 §5.4）。
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

        /** 内腔半宽（geo 6×3.4 → ±3.0 / ±1.7 geo 单位） */
        private static final ContainerFluidSurfaceSpec BASIN_WATER_SPEC =
                new ContainerFluidSurfaceSpec(3.0f / 16f, 1.7f / 16f);

        private SoapMoldDisplaySnapshot snapshot = SoapMoldDisplaySnapshot.from(null);
        private final List<CapturedAnchor> capturedAnchors = new ArrayList<>();
        /** 仅 dedupe 盆内 Item / 水面锚点；勿与 {@code soap_product} 共用。 */
        private final Set<String> basinLayerBonesThisFrame = new HashSet<>();
        private boolean soapAnchorCapturedThisFrame;

        BodyRenderer() {
            super(new SoapMoldBlockGeoModel());
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
            this.basinLayerBonesThisFrame.clear();
            this.soapAnchorCapturedThisFrame = false;

            super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

            for (CapturedAnchor anchor : capturedAnchors) {
                if (anchor.kind() == AnchorKind.ITEM) {
                    anchor.drawItem(poseStack, bufferSource, packedLight, snapshot);
                } else if (anchor.kind() == AnchorKind.SOAP) {
                    anchor.drawSoap(poseStack, bufferSource, packedLight, blockEntity);
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
            if (basinLayerBonesThisFrame.contains(boneName)) {
                return;
            }

            if ("fluid_surface".equals(boneName) && live.showWater()) {
                basinLayerBonesThisFrame.add(boneName);
                capturedAnchors.add(CapturedAnchor.fluid(copyPoseAtPivot(poseStack, bone)));
            }
            if ("soap_product".equals(boneName)
                    && live.contents().phase() == SoapMoldPhase.READY
                    && !soapAnchorCapturedThisFrame) {
                soapAnchorCapturedThisFrame = true;
                capturedAnchors.add(CapturedAnchor.soap(copyPoseAtPivot(poseStack, bone)));
            }
            for (SoapMoldDisplaySnapshot.DisplayItem entry : live.basinItems()) {
                if (entry.anchorBone().equals(boneName)) {
                    basinLayerBonesThisFrame.add(boneName);
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
            FLUID,
            SOAP
        }

        private record CapturedAnchor(
                AnchorKind kind, PoseSnapshot transform, ItemStack stack, String anchorBone) {

            static CapturedAnchor item(PoseSnapshot transform, ItemStack stack, String anchorBone) {
                return new CapturedAnchor(AnchorKind.ITEM, transform, stack, anchorBone);
            }

            static CapturedAnchor fluid(PoseSnapshot transform) {
                return new CapturedAnchor(AnchorKind.FLUID, transform, ItemStack.EMPTY, "fluid_surface");
            }

            static CapturedAnchor soap(PoseSnapshot transform) {
                return new CapturedAnchor(AnchorKind.SOAP, transform, ItemStack.EMPTY, "soap_product");
            }

            void drawItem(
                    PoseStack poseStack,
                    MultiBufferSource bufferSource,
                    int light,
                    SoapMoldDisplaySnapshot snapshot) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                pose.pose().set(transform.pose());
                pose.normal().set(transform.normal());
                SoapMoldBasinItemRenderer.draw(
                        poseStack, bufferSource, light, stack, anchorBone, snapshot);
                poseStack.popPose();
            }

            void drawSoap(
                    PoseStack poseStack,
                    MultiBufferSource bufferSource,
                    int light,
                    SoapMoldBlockEntity blockEntity) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                pose.pose().set(transform.pose());
                pose.normal().set(transform.normal());
                SoapMoldSoapProductRenderer.draw(poseStack, bufferSource, light, blockEntity);
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
                ContainerFluidSurfacePass.enqueue(
                        blockPos,
                        new Matrix4f(pose.pose()),
                        new Matrix3f(pose.normal()),
                        light,
                        BASIN_WATER_SPEC);
                poseStack.popPose();
            }
        }
    }
}
