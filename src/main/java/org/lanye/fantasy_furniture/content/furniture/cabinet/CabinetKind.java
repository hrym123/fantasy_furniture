package org.lanye.fantasy_furniture.content.furniture.cabinet;

/**
 * 柜子型号：三层槽位布局（北向模型空间，原点在方块底心）。
 *
 * <p>点选：相对底格（master）的命中高度，取距层腔中心最近的一层（点哪里放哪里）。
 * <p>渲染：C020 常量表 + FIXED；缩放使外接约空腔 {@link #INTERIOR_FIT}。
 */
public enum CabinetKind {
    CABINET_1(
            "cabinet_1",
            3,
            /* 内宽/深/各层腔高（格） */ 12f / 16f,
            14f / 16f,
            new float[] {12f / 16f, 14f / 16f, 14f / 16f},
            /* 层板顶 Y */ new float[] {2f / 16f, 16f / 16f, 32f / 16f},
            /* 展品 Z（偏后，少探出柜口） */ 2f / 16f),
    CABINET_2(
            "cabinet_2",
            1,
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
    private final int columnParts;
    private final float cavityWidth;
    private final float cavityDepth;
    private final float[] cavityHeight;
    private final float[] shelfTopY;
    private final float itemZ;

    CabinetKind(
            String assetId,
            int columnParts,
            float cavityWidth,
            float cavityDepth,
            float[] cavityHeight,
            float[] shelfTopY,
            float itemZ) {
        this.assetId = assetId;
        this.columnParts = columnParts;
        this.cavityWidth = cavityWidth;
        this.cavityDepth = cavityDepth;
        this.cavityHeight = cavityHeight;
        this.shelfTopY = shelfTopY;
        this.itemZ = itemZ;
    }

    public String assetId() {
        return assetId;
    }

    /** 竖向占地格数（柜子1型=3，柜子2型=1）。 */
    public int columnParts() {
        return columnParts;
    }

    public double heightBlocks() {
        return columnParts;
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
     * 相对底格的命中高度 → 最近层腔（点哪里放哪里）。
     *
     * @param localY 命中点 Y − master 底格 Y（格）
     */
    public CabinetSlot slotFromLocalY(double localY) {
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < CabinetSlot.COUNT; i++) {
            double center = shelfTopY[i] + cavityHeight[i] * 0.5;
            double d = Math.abs(localY - center);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return CabinetSlot.byIndex(best);
    }
}
