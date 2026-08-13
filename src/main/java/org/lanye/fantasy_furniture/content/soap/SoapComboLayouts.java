package org.lanye.fantasy_furniture.content.soap;

import org.lanye.reverie_core.composite.PartLayout;
import org.lanye.reverie_core.composite.PartLayoutTable;
import org.lanye.reverie_core.composite.PartPose;

/**
 * 瓶罐摞载体 / 完成态乳霜：单件 geo 相对默认放置的布局偏移（方块坐标）。
 *
 * <p>{@link SoapBottleCarrierCollisionShapes} 对单件碰撞做 {@link PartPose#transformNorthShape}，与本表同源。
 */
public final class SoapComboLayouts {

    /** 架：默认位 → 完成态陈列位（Δx=-3、Δz=-4 像素） */
    public static final PartPose RACK_DONE = PartPose.translate(-3.0 / 16.0, 0.0, -4.0 / 16.0);

    /** 架：默认位 → 中间态陈列位（Δz=-4 像素） */
    public static final PartPose RACK_MID = PartPose.translate(0.0, 0.0, -4.0 / 16.0);

    /** 盒：默认关盖位 → 完成态陈列位（Δx=-3.5、Δz=-4 像素） */
    public static final PartPose BOX_DONE = PartPose.translate(-3.5 / 16.0, 0.0, -4.0 / 16.0);

    /** 盒：默认关盖位 → 中间态陈列位（Δz=-4 像素） */
    public static final PartPose BOX_MID = PartPose.translate(0.0, 0.0, -4.0 / 16.0);

    /**
     * 单瓶乳霜默认碰撞 {@code box(5.5…10.5)} → 完成态陈列位（原组合标定 {@code box(10,0,2,15,3.75,7)}）。
     * Δ = min 差：(10-5.5, 0, 2-5.5) 像素 → (4.5, 0, -3.5)/16。
     */
    public static final PartPose CREAM_DONE_FROM_SINGLE =
            PartPose.translate(4.5 / 16.0, 0.0, -3.5 / 16.0);

    /**
     * 堆叠位 3 {@code box(9,0,2,…)} → 同上组合位（仅差 Δx=+1 像素）。
     */
    public static final PartPose CREAM_DONE_FROM_SLOT3 = PartPose.translate(1.0 / 16.0, 0.0, 0.0);

    public static final PartLayoutTable TABLE =
            PartLayoutTable.builder()
                    .put(
                            SoapBottleParts.LAYOUT_FREE,
                            PartLayout.builder().build())
                    .put(
                            SoapBottleParts.LAYOUT_CARRIER_MID,
                            PartLayout.builder()
                                    .put(SoapBottleParts.CARRIER, RACK_MID)
                                    .build())
                    .put(
                            SoapBottleParts.LAYOUT_CARRIER_DONE,
                            PartLayout.builder()
                                    .put(SoapBottleParts.CARRIER, RACK_DONE)
                                    .put(SoapBottleParts.bottle(2), CREAM_DONE_FROM_SINGLE)
                                    .build())
                    .fallback(PartLayout.builder().build())
                    .build();

    private SoapComboLayouts() {}

    public static PartPose carrierPose(SoapStackCarrierKind kind, boolean intermediate) {
        return switch (kind) {
            case RACK -> intermediate ? RACK_MID : RACK_DONE;
            case BOX -> intermediate ? BOX_MID : BOX_DONE;
        };
    }
}
