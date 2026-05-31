package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.SoapMoldDisplaySnapshot;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtils;

/**
 * 肥皂模具 BER：Geo 本体 + 水面对（仅水桶）+ 各原料 Item（互不影响）。
 *
 * <p>绘制顺序：Geo 本体 → Item → 水面（提交 translucent 缓冲，不在 BER 内 flush / 改 depthMask）。
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

        private static final ResourceLocation WATER_SPRITE =
                ResourceLocation.withDefaultNamespace("block/water_still");

        private static final float ITEM_SCALE = 0.20f;

        private static final float WATER_HALF_X = 3.0f / 16f;
        private static final float WATER_HALF_Z = 1.7f / 16f;

        private static final float BASIN_CENTER_X = 0f;
        private static final float BASIN_CENTER_Z = 1.5f;

        /** 水面在容器口略下（fluid_surface 锚点 y≈3.6 geo，内腔壁顶 y=4） */
        private static final float WATER_SURFACE_Y = 0.0f;

        private static final float WATER_ALPHA = 0.58f;

        /** 半透明水面；不在 BER 内 endBatch / 改 depthMask，避免破坏同帧 Geo cutout。 */
        private static final RenderType WATER_SURFACE_TYPE =
                RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);

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
                    anchor.draw(poseStack, bufferSource, packedLight, snapshot, partialTick, blockPos);
                }
            }
            for (CapturedAnchor anchor : capturedAnchors) {
                if (anchor.kind() == AnchorKind.WATER) {
                    anchor.draw(poseStack, bufferSource, packedLight, snapshot, partialTick, blockPos);
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

            // 仅主 Geo cutout pass 捕获一次，避免多 RenderType 重复叠绘水面
            ResourceLocation texture = getTextureLocation(blockEntity);
            if (!renderType.equals(RenderType.entityCutoutNoCull(texture))) {
                return;
            }

            // 每骨重新读 BE，避免 blockstate 更新后快照滞后
            SoapMoldDisplaySnapshot live = SoapMoldDisplaySnapshot.from(blockEntity);
            String boneName = bone.getName();
            if (!capturedBonesThisFrame.add(boneName)) {
                return;
            }

            if ("fluid_surface".equals(boneName) && live.showWater()) {
                capturedAnchors.add(CapturedAnchor.water(copyPoseAtPivot(poseStack, bone)));
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
            WATER
        }

        private record CapturedAnchor(
                AnchorKind kind, PoseSnapshot transform, ItemStack stack, String anchorBone) {

            static CapturedAnchor item(PoseSnapshot transform, ItemStack stack, String anchorBone) {
                return new CapturedAnchor(AnchorKind.ITEM, transform, stack, anchorBone);
            }

            static CapturedAnchor water(PoseSnapshot transform) {
                return new CapturedAnchor(AnchorKind.WATER, transform, ItemStack.EMPTY, "fluid_surface");
            }

            void draw(
                    PoseStack poseStack,
                    MultiBufferSource bufferSource,
                    int light,
                    SoapMoldDisplaySnapshot snapshot,
                    float partialTick,
                    BlockPos blockPos) {
                poseStack.pushPose();
                PoseStack.Pose pose = poseStack.last();
                pose.pose().set(transform.pose());
                pose.normal().set(transform.normal());

                if (kind == AnchorKind.ITEM) {
                    drawBasinItem(poseStack, bufferSource, light, stack, anchorBone, snapshot, partialTick);
                } else {
                    drawWaterSurface(poseStack, bufferSource, light, snapshot, partialTick, blockPos);
                }
                poseStack.popPose();
            }
        }

        private static void drawBasinItem(
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                int light,
                ItemStack stack,
                String anchorBone,
                SoapMoldDisplaySnapshot snapshot,
                float partialTick) {
            if (stack.isEmpty()) {
                return;
            }
            boolean curing = snapshot.contents().phase() == SoapMoldPhase.CURING;
            float time =
                    Minecraft.getInstance().level != null
                            ? Minecraft.getInstance().level.getGameTime() + partialTick
                            : partialTick;
            float spin = curing ? Mth.sin(time * 0.08f) * 8f : 0f;
            float bob = curing ? Mth.sin(time * 0.12f) * 0.04f : 0f;

            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            // 底面落锚点后再后仰，避免 lean 轴偏斜导致悬浮
            poseStack.translate(0.0, 0.5f, 0.0);
            applyIngredientLeanBack(poseStack, anchorBone);
            poseStack.translate(0.0, bob, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(
                            stack,
                            ItemDisplayContext.FIXED,
                            light,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            bufferSource,
                            Minecraft.getInstance().level,
                            0);
        }

        private static void applyIngredientLeanBack(PoseStack poseStack, String anchorBone) {
            float px;
            float pz;
            switch (anchorBone) {
                case "ingredient_liquid" -> {
                    px = 2.0f;
                    pz = 0.4f;
                }
                case "ingredient_honey" -> {
                    px = -2.0f;
                    pz = 0.4f;
                }
                case "ingredient_pigment" -> {
                    px = 0f;
                    pz = 2.6f;
                }
                default -> {
                    return;
                }
            }
            float dx = px - BASIN_CENTER_X;
            float dz = pz - BASIN_CENTER_Z;
            float awayYaw = (float) Math.toDegrees(Math.atan2(dx, -dz));
            float dist = Mth.sqrt(dx * dx + dz * dz);
            float pitch = -(12f + dist * 2.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(awayYaw * 0.3f));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        }

        private static void drawWaterSurface(
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                int light,
                SoapMoldDisplaySnapshot snapshot,
                float partialTick,
                BlockPos blockPos) {
            float wobble =
                    snapshot.contents().phase() == SoapMoldPhase.CURING
                            ? Mth.sin((snapshot.contents().cureFinishGameTime() + partialTick) * 0.05f) * 0.015f
                            : 0f;

            poseStack.translate(0.0, WATER_SURFACE_Y + wobble, 0.0);

            TextureAtlasSprite sprite =
                    Minecraft.getInstance()
                            .getModelManager()
                            .getAtlas(InventoryMenu.BLOCK_ATLAS)
                            .getSprite(WATER_SPRITE);

            float red;
            float green;
            float blue;
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                int waterColor = BiomeColors.getAverageWaterColor(level, blockPos);
                red = ((waterColor >> 16) & 0xFF) / 255f;
                green = ((waterColor >> 8) & 0xFF) / 255f;
                blue = (waterColor & 0xFF) / 255f;
                // water_still 贴图偏灰，略压亮度、抬蓝，避免看起来像白膜
                red *= 0.55f;
                green *= 0.75f;
                blue = Mth.clamp(blue * 1.15f + 0.08f, 0f, 1f);
            } else {
                red = 0.18f;
                green = 0.42f;
                blue = 0.92f;
            }

            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();

            VertexConsumer consumer = bufferSource.getBuffer(WATER_SURFACE_TYPE);
            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            addWaterVertex(
                    consumer, pose, normal, -WATER_HALF_X, 0, -WATER_HALF_Z, u0, v0, light, red, green, blue);
            addWaterVertex(
                    consumer, pose, normal, WATER_HALF_X, 0, -WATER_HALF_Z, u1, v0, light, red, green, blue);
            addWaterVertex(
                    consumer, pose, normal, WATER_HALF_X, 0, WATER_HALF_Z, u1, v1, light, red, green, blue);
            addWaterVertex(
                    consumer, pose, normal, -WATER_HALF_X, 0, WATER_HALF_Z, u0, v1, light, red, green, blue);
        }

        private static void addWaterVertex(
                VertexConsumer consumer,
                Matrix4f pose,
                Matrix3f normal,
                float x,
                float y,
                float z,
                float u,
                float v,
                int packedLight,
                float red,
                float green,
                float blue) {
            consumer.vertex(pose, x, y, z)
                    .color(red, green, blue, WATER_ALPHA)
                    .uv(u, v)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(normal, 0f, 1f, 0f)
                    .endVertex();
        }
    }
}
