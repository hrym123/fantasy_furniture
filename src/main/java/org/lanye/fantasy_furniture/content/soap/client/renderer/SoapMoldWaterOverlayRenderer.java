package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 肥皂模具盆内水平水面（炼药锅式单层 quad）。 */
@OnlyIn(Dist.CLIENT)
final class SoapMoldWaterOverlayRenderer {

    private static final ResourceLocation WATER_STILL =
            ResourceLocation.withDefaultNamespace("block/water_still");

    /** 内腔半宽（geo 6×3.4 → ±3.0 / ±1.7 geo 单位） */
    private static final float HALF_X = 3.0f / 16f;
    private static final float HALF_Z = 1.7f / 16f;

    private static final float WATER_ALPHA = 0.58f;

    private SoapMoldWaterOverlayRenderer() {}

    static void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            BlockPos blockPos,
            float surfaceWobble) {
        float y = surfaceWobble;

        TextureAtlasSprite sprite =
                Minecraft.getInstance()
                        .getModelManager()
                        .getAtlas(SoapMoldWaterRenderType.blockAtlas())
                        .getSprite(WATER_STILL);

        float red;
        float green;
        float blue;
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            int waterColor = BiomeColors.getAverageWaterColor(level, blockPos);
            red = ((waterColor >> 16) & 0xFF) / 255f;
            green = ((waterColor >> 8) & 0xFF) / 255f;
            blue = (waterColor & 0xFF) / 255f;
            red *= 0.55f;
            green *= 0.75f;
            blue = Mth.clamp(blue * 1.15f + 0.08f, 0f, 1f);
        } else {
            red = 0.18f;
            green = 0.42f;
            blue = 0.92f;
        }

        VertexConsumer consumer = bufferSource.getBuffer(SoapMoldWaterRenderType.waterSurface());
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        putQuad(
                consumer,
                pose,
                normalMat,
                new Vector3f(-HALF_X, y, -HALF_Z),
                new Vector3f(HALF_X, y, -HALF_Z),
                new Vector3f(HALF_X, y, HALF_Z),
                new Vector3f(-HALF_X, y, HALF_Z),
                0f,
                1f,
                0f,
                u0,
                v0,
                u1,
                v1,
                light,
                red,
                green,
                blue,
                WATER_ALPHA);
    }

    private static void putQuad(
            VertexConsumer consumer,
            Matrix4f pose,
            Matrix3f normalMat,
            Vector3f p0,
            Vector3f p1,
            Vector3f p2,
            Vector3f p3,
            float nx,
            float ny,
            float nz,
            float texU0,
            float texV0,
            float texU1,
            float texV1,
            int light,
            float red,
            float green,
            float blue,
            float alpha) {
        putVertex(consumer, pose, normalMat, p0, nx, ny, nz, texU0, texV0, light, red, green, blue, alpha);
        putVertex(consumer, pose, normalMat, p1, nx, ny, nz, texU1, texV0, light, red, green, blue, alpha);
        putVertex(consumer, pose, normalMat, p2, nx, ny, nz, texU1, texV1, light, red, green, blue, alpha);
        putVertex(consumer, pose, normalMat, p3, nx, ny, nz, texU0, texV1, light, red, green, blue, alpha);
    }

    private static void putVertex(
            VertexConsumer consumer,
            Matrix4f pose,
            Matrix3f normalMat,
            Vector3f pos,
            float nx,
            float ny,
            float nz,
            float u,
            float v,
            int light,
            float red,
            float green,
            float blue,
            float alpha) {
        consumer.vertex(pose, pos.x(), pos.y(), pos.z())
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMat, nx, ny, nz)
                .endVertex();
    }
}
