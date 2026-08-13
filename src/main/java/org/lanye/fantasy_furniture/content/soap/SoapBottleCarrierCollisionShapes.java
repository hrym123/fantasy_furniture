package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 瓶罐摞上架/盒载体北向碰撞（组合陈列位绝对外接盒）。
 *
 * <p>与 {@link SoapComboLayouts} 偏移标定同源；<strong>不是</strong>独立 geo 文件——渲染用单件架/盒 geo。
 */
public final class SoapBottleCarrierCollisionShapes {

    private SoapBottleCarrierCollisionShapes() {}

    /** 完成态架陈列位外接盒（仅碰撞 / 选取） */
    public static final VoxelShape RACK_COMPLETED =
            Block.box(1.00, 0.00, 1.50, 9.00, 1.00, 6.50);

    /** 中间态架陈列位外接盒 */
    public static final VoxelShape RACK_INTERMEDIATE =
            Block.box(4.00, 0.00, 1.50, 12.00, 1.00, 6.50);

    /** 完成态盒陈列位外接盒 */
    public static final VoxelShape BOX_COMPLETED =
            Block.box(0.00, 0.00, 1.00, 9.00, 5.00, 7.00);

    /** 中间态盒陈列位外接盒 */
    public static final VoxelShape BOX_INTERMEDIATE =
            Block.box(3.50, 0.00, 1.00, 12.50, 5.00, 7.00);

    /** 完成态第 3 位乳霜陈列外接盒 */
    public static final VoxelShape COMBO_CREAM =
            Block.box(10.00, 0.00, 2.00, 15.00, 3.75, 7.00);

    public static VoxelShape carrierNorth(SoapStackCarrierKind kind, boolean intermediate) {
        return switch (kind) {
            case RACK -> intermediate ? RACK_INTERMEDIATE : RACK_COMPLETED;
            case BOX -> intermediate ? BOX_INTERMEDIATE : BOX_COMPLETED;
        };
    }
}
