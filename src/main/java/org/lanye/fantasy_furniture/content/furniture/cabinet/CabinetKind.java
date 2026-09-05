package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 柜子型号：三层槽位布局（北向模型空间，原点在方块底心）。
 *
 * <p>点选：视线与整柜 AABB 求交后按高度三等分（不依赖命中面）。
 * <p>渲染：C020 常量表 + FIXED；缩放使外接约空腔 {@link #INTERIOR_FIT}。
 */
public enum CabinetKind {
    CABINET_1(
            "cabinet_1",
            3.0,
            /* 内宽/深/各层腔高（格） */ 12f / 16f,
            14f / 16f,
            new float[] {12f / 16f, 14f / 16f, 14f / 16f},
            /* 层板顶 Y */ new float[] {2f / 16f, 16f / 16f, 32f / 16f},
            /* 展品 Z（偏后，少探出柜口） */ 2f / 16f),
    CABINET_2(
            "cabinet_2",
            1.0,
            14f / 16f,
            6f / 16f,
            new float[] {4f / 16f, 4f / 16f, 4f / 16f},
            new float[] {1f / 16f, 6f / 16f, 11f / 16f},
            4f / 16f);

    public static final float INTERIOR_FIT = 0.80f;
    public static final float SHELF_CLEARANCE = 0.04f;

    /**
     * FIXED 对方块模型约再缩 0.5；外层再乘 {@code 1/FIXED_BLOCK_SCALE} 使视觉边长 ≈ fit。
     */
    public static final float FIXED_BLOCK_SCALE = 0.5f;

    private final String assetId;
    private final double heightBlocks;
    private final float cavityWidth;
    private final float cavityDepth;
    private final float[] cavityHeight;
    private final float[] shelfTopY;
    private final float itemZ;

    CabinetKind(
            String assetId,
            double heightBlocks,
            float cavityWidth,
            float cavityDepth,
            float[] cavityHeight,
            float[] shelfTopY,
            float itemZ) {
        this.assetId = assetId;
        this.heightBlocks = heightBlocks;
        this.cavityWidth = cavityWidth;
        this.cavityDepth = cavityDepth;
        this.cavityHeight = cavityHeight;
        this.shelfTopY = shelfTopY;
        this.itemZ = itemZ;
    }

    public String assetId() {
        return assetId;
    }

    public double heightBlocks() {
        return heightBlocks;
    }

    public float itemZ() {
        return itemZ;
    }

    public float shelfTopY(CabinetSlot slot) {
        return shelfTopY[slot.index()];
    }

    /** 该层目标外接边长（取空腔宽高深最小值 × 80%）。 */
    public float fitSize(CabinetSlot slot) {
        float h = Math.max(0.05f, cavityHeight[slot.index()] - SHELF_CLEARANCE);
        return Math.min(cavityWidth, Math.min(h, cavityDepth)) * INTERIOR_FIT;
    }

    /**
     * 展品中心 Y：层板顶 + 间隙 + 半个 fit（直立放在层板上）。
     */
    public float itemCenterY(CabinetSlot slot) {
        float fit = fitSize(slot);
        return shelfTopY(slot) + SHELF_CLEARANCE + fit * 0.5f;
    }

    /**
     * 视线与整柜外包盒求交，按高度三等分得到槽位（稳定，不依赖点到顶/侧面）。
     */
    public CabinetSlot slotFromLook(BlockPos pos, Player player) {
        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = eye.add(player.getViewVector(1f).scale(8.0));
        AABB box =
                new AABB(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        pos.getX() + 1.0,
                        pos.getY() + heightBlocks,
                        pos.getZ() + 1.0);
        Optional<Vec3> hit = box.clip(eye, end);
        double localY = hit.map(v -> v.y - pos.getY()).orElse(heightBlocks * 0.5);
        double t = Mth.clamp(localY / heightBlocks, 0.0, 0.999);
        return CabinetSlot.byIndex((int) (t * CabinetSlot.COUNT));
    }
}
