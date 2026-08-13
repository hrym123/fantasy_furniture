package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 瓶罐摞上架/盒载体北向碰撞（由组合 geo {@code geo_collision_box.py --gecko-block} 外接盒）。
 */
public final class SoapBottleCarrierCollisionShapes {

    private SoapBottleCarrierCollisionShapes() {}

    /** {@code soap_bottle_combo_rack.geo.json} */
    public static final VoxelShape RACK_COMPLETED =
            Block.box(1.00, 0.00, 1.50, 9.00, 1.00, 6.50);

    /** {@code soap_bottle_combo_rack2.geo.json} */
    public static final VoxelShape RACK_INTERMEDIATE =
            Block.box(4.00, 0.00, 1.50, 12.00, 1.00, 6.50);

    /** {@code soap_bottle_combo_box.geo.json} */
    public static final VoxelShape BOX_COMPLETED =
            Block.box(0.00, 0.00, 1.00, 9.00, 5.00, 7.00);

    /** {@code soap_bottle_combo_box2.geo.json} */
    public static final VoxelShape BOX_INTERMEDIATE =
            Block.box(3.50, 0.00, 1.00, 12.50, 5.00, 7.00);

    /** {@code soap_bottle_combo_cream.geo.json}（完成态第 3 位乳霜） */
    public static final VoxelShape COMBO_CREAM =
            Block.box(10.00, 0.00, 2.00, 15.00, 3.75, 7.00);

    public static VoxelShape carrierNorth(SoapStackCarrierKind kind, boolean intermediate) {
        return switch (kind) {
            case RACK -> intermediate ? RACK_INTERMEDIATE : RACK_COMPLETED;
            case BOX -> intermediate ? BOX_INTERMEDIATE : BOX_COMPLETED;
        };
    }
}
