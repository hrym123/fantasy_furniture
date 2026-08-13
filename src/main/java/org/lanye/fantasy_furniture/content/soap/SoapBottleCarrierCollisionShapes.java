package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 瓶罐摞上架/盒 / 完成态乳霜北向碰撞：与<strong>单独放置</strong>同形，再经 {@link SoapComboLayouts} 平移到位。
 *
 * <p>渲染亦为单件 geo + 同表偏移，故碰撞与可见模型一致。
 */
public final class SoapBottleCarrierCollisionShapes {

    private SoapBottleCarrierCollisionShapes() {}

    /** 与 {@code SoapRackBlock} 北向一致 */
    private static final VoxelShape RACK_STANDALONE_NORTH = Block.box(4.0, 0.0, 5.5, 12.0, 1.0, 10.5);

    /** 与 {@code SoapBoxBlock} 关盖北向一致（载体 overlay 用关盖 geo） */
    private static final VoxelShape BOX_CLOSED_STANDALONE_NORTH =
            Block.box(3.5, 0.0, 5.0, 12.5, 4.0, 11.0);

    /** 完成态架陈列位 */
    public static final VoxelShape RACK_COMPLETED =
            SoapComboLayouts.RACK_DONE.transformNorthShape(RACK_STANDALONE_NORTH);

    /** 中间态架陈列位 */
    public static final VoxelShape RACK_INTERMEDIATE =
            SoapComboLayouts.RACK_MID.transformNorthShape(RACK_STANDALONE_NORTH);

    /** 完成态盒陈列位 */
    public static final VoxelShape BOX_COMPLETED =
            SoapComboLayouts.BOX_DONE.transformNorthShape(BOX_CLOSED_STANDALONE_NORTH);

    /** 中间态盒陈列位 */
    public static final VoxelShape BOX_INTERMEDIATE =
            SoapComboLayouts.BOX_MID.transformNorthShape(BOX_CLOSED_STANDALONE_NORTH);

    /**
     * 完成态第 3 位乳霜：单瓶乳霜碰撞 + {@link SoapComboLayouts#CREAM_DONE_FROM_SINGLE}（与 overlay 同源）。
     */
    public static final VoxelShape COMBO_CREAM =
            SoapComboLayouts.CREAM_DONE_FROM_SINGLE.transformNorthShape(
                    SoapStackCollisionShapes.bodyCreamNorth(1));

    public static VoxelShape carrierNorth(SoapStackCarrierKind kind, boolean intermediate) {
        return switch (kind) {
            case RACK -> intermediate ? RACK_INTERMEDIATE : RACK_COMPLETED;
            case BOX -> intermediate ? BOX_INTERMEDIATE : BOX_COMPLETED;
        };
    }
}
