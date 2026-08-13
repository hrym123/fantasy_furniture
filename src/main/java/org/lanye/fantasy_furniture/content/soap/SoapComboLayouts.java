package org.lanye.fantasy_furniture.content.soap;

import org.lanye.reverie_core.composite.PartLayout;
import org.lanye.reverie_core.composite.PartLayoutTable;
import org.lanye.reverie_core.composite.PartPose;

/**
 * 瓶罐摞载体 / 完成态乳霜：单件 geo 相对默认放置的布局偏移（方块坐标）。
 *
 * <p>由单件碰撞盒 min 与组合陈列碰撞盒 min 之差标定，与 {@link SoapBottleCarrierCollisionShapes} 同源。
 */
public final class SoapComboLayouts {

    /**
     * 架：standalone {@code box(4,0,5.5,12,1,10.5)} → 完成态 {@code box(1,0,1.5,9,1,6.5)}
     */
    public static final PartPose RACK_DONE = PartPose.translate(-3.0 / 16.0, 0.0, -4.0 / 16.0);

    /** 架：→ 中间态 {@code box(4,0,1.5,12,1,6.5)} */
    public static final PartPose RACK_MID = PartPose.translate(0.0, 0.0, -4.0 / 16.0);

    /**
     * 盒：standalone 关盖 {@code box(3.5,0,5,12.5,4,11)} → 完成态 {@code box(0,0,1,9,5,7)}
     */
    public static final PartPose BOX_DONE = PartPose.translate(-3.5 / 16.0, 0.0, -4.0 / 16.0);

    /** 盒：→ 中间态 {@code box(3.5,0,1,12.5,5,7)} */
    public static final PartPose BOX_MID = PartPose.translate(0.0, 0.0, -4.0 / 16.0);

    /**
     * 完成态第 3 位乳霜：堆叠位 3 {@code box(9,0,2,…)} → 组合位 {@code box(10,0,2,…)}；
     * 若用<strong>单瓶</strong>默认 geo（近位 1），则另见 {@link #CREAM_DONE_FROM_SINGLE}。
     */
    public static final PartPose CREAM_DONE_FROM_SLOT3 = PartPose.translate(1.0 / 16.0, 0.0, 0.0);

    /**
     * 单瓶乳霜默认（近位 1 {@code box(9,0,9,…)}）→ 组合乳霜位 {@code box(10,0,2,…)}。
     */
    public static final PartPose CREAM_DONE_FROM_SINGLE =
            PartPose.translate(1.0 / 16.0, 0.0, -7.0 / 16.0);

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
