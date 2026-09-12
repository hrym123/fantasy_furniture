package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 柜子型号：槽位布局（北向模型空间，原点在方块底心）。
 *
 * <p>柜子1型：竖向 3 格 × 1 列；柜子2型：单格内 3×3（与 geo 分隔条对齐）。
 * <p>点选：柜子1 用 {@code PART}（勿改）；柜子2 优先用方块命中面 XY，否则准心落开口平面。
 */
public enum CabinetKind {
    CABINET_1(
            "cabinet_1",
            3,
            3,
            1,
            /* 内宽/深 */ 12f / 16f,
            14f / 16f,
            /* 各行腔高 */ new float[] {12f / 16f, 14f / 16f, 14f / 16f},
            /* 层板顶 Y */ new float[] {2f / 16f, 16f / 16f, 32f / 16f},
            /* 展品 Z */ -1f / 16f),
    CABINET_2(
            "cabinet_2",
            1,
            3,
            3,
            14f / 16f,
            6f / 16f,
            /* geo 腔高 4px */ new float[] {4f / 16f, 4f / 16f, 4f / 16f},
            new float[] {1f / 16f, 6f / 16f, 11f / 16f},
            4f / 16f);

    /**
     * 柜子2 geo 真实空腔（像素）：列 [-7,-3]/[-2,2]/[3,7]，行 [1,5]/[6,10]/[11,15]，深 [1,7]。
     * 点选 AABB 扩到分隔条中线，避免准心贴分隔条时射不中任何格。
     */
    private static final float[] C2_COL_MIN = {-7f / 16f, -2f / 16f, 3f / 16f};
    private static final float[] C2_COL_MAX = {-3f / 16f, 2f / 16f, 7f / 16f};
    private static final float[] C2_PICK_COL_MIN = {-7f / 16f, -2.5f / 16f, 2.5f / 16f};
    private static final float[] C2_PICK_COL_MAX = {-2.5f / 16f, 2.5f / 16f, 7f / 16f};
    private static final float[] C2_ROW_MIN = {1f / 16f, 6f / 16f, 11f / 16f};
    private static final float[] C2_ROW_MAX = {5f / 16f, 10f / 16f, 15f / 16f};
    private static final float[] C2_PICK_ROW_MIN = {1f / 16f, 5.5f / 16f, 10.5f / 16f};
    private static final float[] C2_PICK_ROW_MAX = {5.5f / 16f, 10.5f / 16f, 15f / 16f};
    private static final float C2_Z_MIN = 1f / 16f;
    private static final float C2_Z_MAX = 7f / 16f;

    public static final float INTERIOR_FIT = 0.80f;
    public static final float SHELF_CLEARANCE = 0.02f;

    /**
     * 格内可用外接尺寸（已乘 {@link #INTERIOR_FIT}；高度已扣 {@link #SHELF_CLEARANCE}）。
     * 展品应按 W/H/D 分别拟合后取统一缩放 {@code min(fitW/sx, fitH/sy, fitD/sz)}。
     */
    public record CavityFit(float width, float height, float depth) {
        /** 三轴中的最小边，供仍只需标量 fit 的调用方。 */
        public float min() {
            return Math.min(width, Math.min(height, depth));
        }
    }

    private final String assetId;
    private final int columnParts;
    private final int rows;
    private final int cols;
    private final float cavityWidth;
    private final float cavityDepth;
    private final float[] cavityHeight;
    private final float[] shelfTopY;
    private final float itemZ;

    CabinetKind(
            String assetId,
            int columnParts,
            int rows,
            int cols,
            float cavityWidth,
            float cavityDepth,
            float[] cavityHeight,
            float[] shelfTopY,
            float itemZ) {
        this.assetId = assetId;
        this.columnParts = columnParts;
        this.rows = rows;
        this.cols = cols;
        this.cavityWidth = cavityWidth;
        this.cavityDepth = cavityDepth;
        this.cavityHeight = cavityHeight;
        this.shelfTopY = shelfTopY;
        this.itemZ = itemZ;
    }

    public String assetId() {
        return assetId;
    }

    public int columnParts() {
        return columnParts;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public int slotCount() {
        return rows * cols;
    }

    public double heightBlocks() {
        return columnParts;
    }

    public float itemZ() {
        return itemZ;
    }

    public int rowOf(int slot) {
        return slot / cols;
    }

    public int colOf(int slot) {
        return slot % cols;
    }

    public float shelfTopY(int slot) {
        if (this == CABINET_2) {
            return C2_ROW_MIN[rowOf(slot)];
        }
        return shelfTopY[rowOf(slot)];
    }

    /** 该格水平中心 X（北向；柜子2 对齐 geo 空腔中心）。 */
    public float itemX(int slot) {
        if (this == CABINET_2) {
            int c = colOf(slot);
            return (C2_COL_MIN[c] + C2_COL_MAX[c]) * 0.5f;
        }
        return 0f;
    }

    /** 该格可用外接 W/H/D（已乘 INTERIOR_FIT）。 */
    public CavityFit cavityFit(int slot) {
        if (this == CABINET_2) {
            int c = colOf(slot);
            int r = rowOf(slot);
            float w = (C2_COL_MAX[c] - C2_COL_MIN[c]) * INTERIOR_FIT;
            float h = Math.max(0.05f, C2_ROW_MAX[r] - C2_ROW_MIN[r] - SHELF_CLEARANCE) * INTERIOR_FIT;
            float d = (C2_Z_MAX - C2_Z_MIN) * INTERIOR_FIT;
            return new CavityFit(w, h, d);
        }
        float cellW = (cavityWidth / Math.max(1, cols)) * INTERIOR_FIT;
        float h = Math.max(0.05f, cavityHeight[rowOf(slot)] - SHELF_CLEARANCE) * INTERIOR_FIT;
        float d = cavityDepth * INTERIOR_FIT;
        return new CavityFit(cellW, h, d);
    }

    public float cavityFitWidth(int slot) {
        return cavityFit(slot).width();
    }

    public float cavityFitHeight(int slot) {
        return cavityFit(slot).height();
    }

    public float cavityFitDepth(int slot) {
        return cavityFit(slot).depth();
    }

    /** 三轴可用尺寸之最小边（兼容旧调用）。 */
    public float fitSize(int slot) {
        return cavityFit(slot).min();
    }

    public float itemFloorY(int slot) {
        return shelfTopY(slot) + SHELF_CLEARANCE;
    }

    /** 点选用空腔盒（含半分隔条；Z 覆盖碰撞厚度）。 */
    private AABB pickVolume(int slot) {
        int c = colOf(slot);
        int r = rowOf(slot);
        return new AABB(
                C2_PICK_COL_MIN[c],
                C2_PICK_ROW_MIN[r],
                C2_Z_MIN,
                C2_PICK_COL_MAX[c],
                C2_PICK_ROW_MAX[r],
                8f / 16f);
    }

    /** 北向局部 XY → 槽位（柜子2）。按 geo 分隔中线划分，并钳制到外框内。 */
    public int slotFromLocal(double localX, double localY) {
        // 命中面 XY 与 Gecko 模型 +X 左右相反，点选时取反以与目视一致
        localX = Mth.clamp(-localX, -7f / 16f, 7f / 16f);
        localY = Mth.clamp(localY, 1f / 16f, 15f / 16f);
        int col = localX < -2.5f / 16f ? 0 : (localX < 2.5f / 16f ? 1 : 2);
        int row = localY < 5.5f / 16f ? 0 : (localY < 10.5f / 16f ? 1 : 2);
        return row * cols + col;
    }

    /**
     * 柜子2 点选（柜子1 勿调用）。
     *
     * <ol>
     *   <li>点在柜正面（{@code hitFace == facing}）：命中点已在开口平面，直接用 XY
     *   <li>否则准心射线与各格盒求交
     *   <li>再否则落到开口平面后再划分
     * </ol>
     */
    public int slotFromHit(
            BlockPos master,
            Direction facing,
            BlockHitResult hit,
            Vec3 eyeWorld,
            Vec3 lookWorld) {
        if (hit.getDirection() == facing) {
            Vec3 local = toNorthLocal(master, facing, hit.getLocation());
            return slotFromLocal(local.x, local.y);
        }

        Vec3 eye = toNorthLocal(master, facing, eyeWorld);
        Vec3 look = lookToNorth(facing, lookWorld);
        double lenSq = look.lengthSqr();
        if (lenSq < 1.0e-8) {
            Vec3 local = toNorthLocal(master, facing, hit.getLocation());
            return slotFromLocal(local.x, local.y);
        }
        Vec3 dir = look.scale(1.0 / Math.sqrt(lenSq));
        Vec3 end = eye.add(dir.scale(12.0));

        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < slotCount(); i++) {
            Optional<Vec3> pt = pickVolume(i).clip(eye, end);
            if (pt.isEmpty()) {
                continue;
            }
            double d = eye.distanceToSqr(pt.get());
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        if (best >= 0) {
            int row = best / cols;
            int col = best % cols;
            // 与 slotFromLocal 相同：点选左右相对模型取反
            return row * cols + (cols - 1 - col);
        }

        if (Math.abs(dir.z) > 1.0e-4) {
            double t = (C2_Z_MIN - eye.z) / dir.z;
            if (t > 0.0 && t < 64.0) {
                return slotFromLocal(eye.x + dir.x * t, eye.y + dir.y * t);
            }
        }
        Vec3 local = toNorthLocal(master, facing, hit.getLocation());
        return slotFromLocal(local.x, local.y);
    }

    public static Vec3 toNorthLocal(BlockPos master, Direction facing, Vec3 hit) {
        double wx = hit.x - master.getX() - 0.5;
        double wy = hit.y - master.getY();
        double wz = hit.z - master.getZ() - 0.5;
        return rotateHorizontalToNorth(facing, wx, wy, wz);
    }

    public static Vec3 lookToNorth(Direction facing, Vec3 look) {
        return rotateHorizontalToNorth(facing, look.x, look.y, look.z);
    }

    /** 与 Gecko {@code rotateBlock} 水平角一致：N0 / S180 / W90 / E270。 */
    private static Vec3 rotateHorizontalToNorth(Direction facing, double x, double y, double z) {
        return switch (facing) {
            case SOUTH -> new Vec3(-x, y, -z);
            case WEST -> new Vec3(z, y, -x);
            case EAST -> new Vec3(-z, y, x);
            default -> new Vec3(x, y, z);
        };
    }
}
