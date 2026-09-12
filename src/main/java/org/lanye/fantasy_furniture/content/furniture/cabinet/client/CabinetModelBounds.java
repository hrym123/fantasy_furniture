package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * 从 {@link BakedModel} 四边形估包围盒，并按 display 变换后贴层板底边、水平居中。
 */
@OnlyIn(Dist.CLIENT)
final class CabinetModelBounds {

    private static final RandomSource RANDOM = RandomSource.create(42L);
    /** 无几何时退回单位立方体（方块模型常用）。 */
    private static final AABB UNIT = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    private CabinetModelBounds() {}

    static AABB fromModel(@Nullable BakedModel model) {
        return fromModel(model, null);
    }

    /**
     * @param state 方块网格用真实 {@link BlockState}；物品侧传 {@code null}
     */
    static AABB fromModel(@Nullable BakedModel model, @Nullable BlockState state) {
        if (model == null) {
            return UNIT;
        }
        Bounds b = new Bounds();
        for (Direction dir : Direction.values()) {
            accumulate(model.getQuads(state, dir, RANDOM), b);
        }
        accumulate(model.getQuads(state, null, RANDOM), b);
        return b.any ? b.toAabb() : UNIT;
    }

    /**
     * 经指定 display 变换后的包围盒（尚未乘柜内 fit 缩放）。
     *
     * <p>须与 {@link net.minecraft.client.renderer.entity.ItemRenderer} 一致：apply display 后再
     * {@code translate(-0.5,-0.5,-0.5)}（模型四边形在 0～1 空间）。漏掉后者会导致盾/旗/箱等 FIXED
     * 展品严重偏出槽位。
     */
    static AABB afterDisplay(BakedModel model, ItemDisplayContext context) {
        AABB raw = fromModel(model);
        ItemTransform transform = model.getTransforms().getTransform(context);
        PoseStack probe = new PoseStack();
        transform.apply(false, probe);
        probe.translate(-0.5f, -0.5f, -0.5f);
        return transformAabb(raw, new Matrix4f(probe.last().pose()));
    }

    /**
     * 使包围盒最大边缩放到 {@code targetMax}（柜内目标外接边长；优先用 {@link #scaleToFit3D}）。
     */
    static float scaleToFit(AABB bounds, float targetMax) {
        float extent = maxExtent(bounds);
        return targetMax / Math.max(0.01f, extent);
    }

    /**
     * 统一缩放：按空腔可用宽/高/深分别拟合后取最小比，保证不撑破格。
     */
    /** Block/FIXED/Geo AABB often underestimates mesh → pad sizes so scale stays under cubby. */
    private static final float MODEL_CONTENT_PAD = 1.12f;

    static float scaleToFit3D(AABB bounds, float fitW, float fitH, float fitD) {
        return uniformScaleToFit(
                (float) bounds.getXsize() * MODEL_CONTENT_PAD,
                (float) bounds.getYsize() * MODEL_CONTENT_PAD,
                (float) bounds.getZsize() * MODEL_CONTENT_PAD,
                fitW,
                fitH,
                fitD);
    }

    /** 已知内容外接尺寸时的三轴拟合统一缩放。 */
    static float uniformScaleToFit(
            float sizeX, float sizeY, float sizeZ, float fitW, float fitH, float fitD) {
        float sx = fitW / Math.max(0.01f, sizeX);
        float sy = fitH / Math.max(0.01f, sizeY);
        float sz = fitD / Math.max(0.01f, sizeZ);
        return Math.min(sx, Math.min(sy, sz));
    }

    static float maxExtent(AABB bounds) {
        return (float)
                Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
    }

    /**
     * 纯物品：先按 display 后包围盒贴底并水平居中，再统一缩放。
     *
     * <p>顺序：{@code scale(S)} → {@code translate(-midX, -minY, -midZ)} → 内部再施加 FIXED。
     * 贴底：缩放后内容 minY → 0（层板原点由调用方 {@code itemFloorY} 提供）。
     */
    static void translateToFloorCentered(PoseStack poseStack, AABB afterDisplay, float uniformScale) {
        double midX = (afterDisplay.minX + afterDisplay.maxX) * 0.5;
        double midZ = (afterDisplay.minZ + afterDisplay.maxZ) * 0.5;
        poseStack.scale(uniformScale, uniformScale, uniformScale);
        poseStack.translate(-midX, -afterDisplay.minY, -midZ);
    }

    /** 方块网格：按模型包围盒贴底并水平居中后缩到目标边长。 */
    static void translateBlockToFloorCentered(PoseStack poseStack, AABB modelBounds, float uniformScale) {
        double midX = (modelBounds.minX + modelBounds.maxX) * 0.5;
        double midZ = (modelBounds.minZ + modelBounds.maxZ) * 0.5;
        poseStack.scale(uniformScale, uniformScale, uniformScale);
        poseStack.translate(-midX, -modelBounds.minY, -midZ);
    }

    private static void accumulate(List<BakedQuad> quads, Bounds b) {
        for (BakedQuad quad : quads) {
            int[] v = quad.getVertices();
            int stride = v.length / 4;
            for (int i = 0; i < 4; i++) {
                int o = i * stride;
                b.include(
                        Float.intBitsToFloat(v[o]),
                        Float.intBitsToFloat(v[o + 1]),
                        Float.intBitsToFloat(v[o + 2]));
            }
        }
    }

    private static AABB transformAabb(AABB box, Matrix4f mat) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        Vector4f v = new Vector4f();
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? box.minX : box.maxX;
            double y = (i & 2) == 0 ? box.minY : box.maxY;
            double z = (i & 4) == 0 ? box.minZ : box.maxZ;
            v.set((float) x, (float) y, (float) z, 1f);
            mat.transform(v);
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);
            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static final class Bounds {
        boolean any;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        void include(float x, float y, float z) {
            any = true;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        AABB toAabb() {
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
