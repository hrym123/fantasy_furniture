package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;

/**
 * 瓶罐摞上架/盒 overlay 的 PoseStack 平移（方块单位，相对摞原点）。
 *
 * <p>临时可用偏移；正式应对齐 moonstarfish 组合目录 {@code 肥皂架.bbmodel} / {@code 肥皂盒.bbmodel} /
 * {@code 肥皂架2.bbmodel} / {@code 肥皂盒2.bbmodel} 导出。
 */
public final class SoapBottleCarrierRenderOffsets {

    private SoapBottleCarrierRenderOffsets() {}

    /** 完成态（特殊 2）：架 */
    public static final float[] RACK_COMPLETED = {0.35f, 0.0f, 0.0f};

    /** 完成态（特殊 2）：盒 */
    public static final float[] BOX_COMPLETED = {0.35f, 0.0f, 0.0f};

    /** 中间态（特殊 3）：架（*2 语义，略不同偏移） */
    public static final float[] RACK_INTERMEDIATE = {0.28f, 0.0f, 0.08f};

    /** 中间态（特殊 3）：盒 */
    public static final float[] BOX_INTERMEDIATE = {0.28f, 0.0f, 0.08f};

    public static float[] offset(SoapStackCarrierKind kind, boolean intermediate) {
        return switch (kind) {
            case RACK -> intermediate ? RACK_INTERMEDIATE : RACK_COMPLETED;
            case BOX -> intermediate ? BOX_INTERMEDIATE : BOX_COMPLETED;
        };
    }
}
