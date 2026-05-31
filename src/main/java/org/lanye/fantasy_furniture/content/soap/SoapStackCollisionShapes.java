package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.StackLayerCollisions;

/**
 * 肥皂系可堆叠方块北向碰撞：生成时按层数预合并为常量，{@code getShape} 查表 O(1)。
 * 由 工具库/reverie/moonstarfish/_gen_soap_stack_collisions.py 生成，勿手改。
 */
public final class SoapStackCollisionShapes {

    private SoapStackCollisionShapes() {}

    private static VoxelShape northByCount(VoxelShape[] table, int layers) {
        if (layers <= 0 || table.length == 0) {
            return Shapes.empty();
        }
        return table[Math.min(layers, table.length) - 1];
    }

    /** soap_paper_bag.geo.json 北向 · 外接盒 */
    static final VoxelShape SOAP_PAPER_BAG_SINGLE_NORTH =
            Block.box(3.16, 0.00, 6.00, 12.84, 2.50, 10.00);

    /** 层数 1…4 预合并北向碰撞；block1 zmax 贴格后缘；外接盒；下标 layers-1 */
    private static final VoxelShape[] SOAP_PAPER_BAG_NORTH_BY_COUNT = {
            // 1 层
                        Block.box(3.16, 0.00, 6.00, 12.84, 2.50, 10.00),
            // 2 层
                        Block.box(3.16, 0.00, 7.60, 12.84, 4.53, 16.00),
            // 3 层
                        Block.box(3.16, 0.00, 4.10, 12.84, 4.53, 16.00),
            // 4 层
                        Block.box(3.16, 0.00, 0.60, 12.84, 4.53, 16.00),
    };

    /** body_cream.geo.json 北向 + gecko X 镜像 */
    static final VoxelShape BODY_CREAM_SINGLE_NORTH =
            StackLayerCollisions.orParts(
                    Block.box(5.50, 0.00, 5.50, 10.50, 3.00, 10.50),
                    Block.box(5.75, 3.00, 5.75, 10.25, 3.75, 10.25));

    /** 层数 1…5 预合并北向碰撞 + gecko X 镜像；下标 layers-1 */
    private static final VoxelShape[] BODY_CREAM_NORTH_BY_COUNT = {
            // 1 层
                        StackLayerCollisions.orParts(
                    Block.box(5.50, 0.00, 5.50, 10.50, 3.00, 10.50),
                    Block.box(5.75, 3.00, 5.75, 10.25, 3.75, 10.25)),
            // 2 层
                        StackLayerCollisions.orParts(
                    Block.box(2.00, 0.00, 9.00, 7.00, 3.00, 14.00),
                    Block.box(2.25, 3.00, 9.25, 6.75, 3.75, 13.75),
                    Block.box(9.00, 0.00, 9.00, 14.00, 3.00, 14.00),
                    Block.box(9.25, 3.00, 9.25, 13.75, 3.75, 13.75)),
            // 3 层
                        StackLayerCollisions.orParts(
                    Block.box(2.00, 0.00, 9.00, 7.00, 3.00, 14.00),
                    Block.box(2.25, 3.00, 9.25, 6.75, 3.75, 13.75),
                    Block.box(9.00, 0.00, 9.00, 14.00, 3.00, 14.00),
                    Block.box(9.25, 3.00, 9.25, 13.75, 3.75, 13.75),
                    Block.box(2.00, 0.00, 2.00, 7.00, 3.00, 7.00),
                    Block.box(2.25, 3.00, 2.25, 6.75, 3.75, 6.75)),
            // 4 层
                        StackLayerCollisions.orParts(
                    Block.box(2.00, 0.00, 9.00, 7.00, 3.00, 14.00),
                    Block.box(2.25, 3.00, 9.25, 6.75, 3.75, 13.75),
                    Block.box(9.00, 0.00, 9.00, 14.00, 3.00, 14.00),
                    Block.box(9.25, 3.00, 9.25, 13.75, 3.75, 13.75),
                    Block.box(2.00, 0.00, 2.00, 7.00, 3.00, 7.00),
                    Block.box(2.25, 3.00, 2.25, 6.75, 3.75, 6.75),
                    Block.box(9.00, 0.00, 2.00, 14.00, 3.00, 7.00),
                    Block.box(9.25, 3.00, 2.25, 13.75, 3.75, 6.75)),
            // 5 层
                        StackLayerCollisions.orParts(
                    Block.box(2.00, 0.00, 9.00, 7.00, 3.00, 14.00),
                    Block.box(2.25, 3.00, 9.25, 6.75, 3.75, 13.75),
                    Block.box(9.00, 0.00, 9.00, 14.00, 3.00, 14.00),
                    Block.box(9.25, 3.00, 9.25, 13.75, 3.75, 13.75),
                    Block.box(2.00, 0.00, 2.00, 7.00, 3.00, 7.00),
                    Block.box(2.25, 3.00, 2.25, 6.75, 3.75, 6.75),
                    Block.box(9.00, 0.00, 2.00, 14.00, 3.00, 7.00),
                    Block.box(9.25, 3.00, 2.25, 13.75, 3.75, 6.75),
                    Block.box(5.50, 3.70, 5.50, 10.50, 6.70, 10.50),
                    Block.box(5.75, 6.70, 5.75, 10.25, 7.45, 10.25)),
    };

    /** body_wash.geo.json 北向 + gecko X 镜像 */
    static final VoxelShape BODY_WASH_SINGLE_NORTH =
            StackLayerCollisions.orParts(
                    Block.box(5.00, 0.00, 5.00, 11.00, 10.00, 11.00),
                    Block.box(5.50, 10.00, 5.50, 10.50, 10.50, 10.50),
                    Block.box(7.50, 12.40, 5.70, 8.50, 13.00, 7.00),
                    Block.box(7.30, 10.50, 7.30, 8.70, 12.00, 8.70),
                    Block.box(6.70, 12.00, 6.70, 9.30, 13.20, 9.30));

    /** 层数 1…4 预合并北向碰撞 + gecko X 镜像；下标 layers-1 */
    private static final VoxelShape[] BODY_WASH_NORTH_BY_COUNT = {
            // 1 层
                        StackLayerCollisions.orParts(
                    Block.box(5.00, 0.00, 5.00, 11.00, 10.00, 11.00),
                    Block.box(5.50, 10.00, 5.50, 10.50, 10.50, 10.50),
                    Block.box(7.50, 12.40, 5.70, 8.50, 13.00, 7.00),
                    Block.box(7.30, 10.50, 7.30, 8.70, 12.00, 8.70),
                    Block.box(6.70, 12.00, 6.70, 9.30, 13.20, 9.30)),
            // 2 层
                        StackLayerCollisions.orParts(
                    Block.box(1.00, 0.00, 9.00, 7.00, 10.00, 15.00),
                    Block.box(1.50, 10.00, 9.50, 6.50, 10.50, 14.50),
                    Block.box(3.30, 10.50, 11.30, 4.70, 12.00, 12.70),
                    Block.box(3.50, 12.40, 9.70, 4.50, 13.00, 11.00),
                    Block.box(2.70, 12.00, 10.70, 5.30, 13.20, 13.30),
                    Block.box(9.00, 0.00, 9.00, 15.00, 10.00, 15.00),
                    Block.box(9.50, 10.00, 9.50, 14.50, 10.50, 14.50),
                    Block.box(11.30, 10.50, 11.30, 12.70, 12.00, 12.70),
                    Block.box(11.50, 12.40, 9.70, 12.50, 13.00, 11.00),
                    Block.box(10.70, 12.00, 10.70, 13.30, 13.20, 13.30)),
            // 3 层
                        StackLayerCollisions.orParts(
                    Block.box(1.00, 0.00, 9.00, 7.00, 10.00, 15.00),
                    Block.box(1.50, 10.00, 9.50, 6.50, 10.50, 14.50),
                    Block.box(3.30, 10.50, 11.30, 4.70, 12.00, 12.70),
                    Block.box(3.50, 12.40, 9.70, 4.50, 13.00, 11.00),
                    Block.box(2.70, 12.00, 10.70, 5.30, 13.20, 13.30),
                    Block.box(9.00, 0.00, 9.00, 15.00, 10.00, 15.00),
                    Block.box(9.50, 10.00, 9.50, 14.50, 10.50, 14.50),
                    Block.box(11.30, 10.50, 11.30, 12.70, 12.00, 12.70),
                    Block.box(11.50, 12.40, 9.70, 12.50, 13.00, 11.00),
                    Block.box(10.70, 12.00, 10.70, 13.30, 13.20, 13.30),
                    Block.box(1.00, 0.00, 1.00, 7.00, 10.00, 7.00),
                    Block.box(1.50, 10.00, 1.50, 6.50, 10.50, 6.50),
                    Block.box(3.30, 10.50, 3.30, 4.70, 12.00, 4.70),
                    Block.box(3.50, 12.40, 1.70, 4.50, 13.00, 3.00),
                    Block.box(2.70, 12.00, 2.70, 5.30, 13.20, 5.30)),
            // 4 层
                        StackLayerCollisions.orParts(
                    Block.box(1.00, 0.00, 9.00, 7.00, 10.00, 15.00),
                    Block.box(1.50, 10.00, 9.50, 6.50, 10.50, 14.50),
                    Block.box(3.30, 10.50, 11.30, 4.70, 12.00, 12.70),
                    Block.box(3.50, 12.40, 9.70, 4.50, 13.00, 11.00),
                    Block.box(2.70, 12.00, 10.70, 5.30, 13.20, 13.30),
                    Block.box(9.00, 0.00, 9.00, 15.00, 10.00, 15.00),
                    Block.box(9.50, 10.00, 9.50, 14.50, 10.50, 14.50),
                    Block.box(11.30, 10.50, 11.30, 12.70, 12.00, 12.70),
                    Block.box(11.50, 12.40, 9.70, 12.50, 13.00, 11.00),
                    Block.box(10.70, 12.00, 10.70, 13.30, 13.20, 13.30),
                    Block.box(1.00, 0.00, 1.00, 7.00, 10.00, 7.00),
                    Block.box(1.50, 10.00, 1.50, 6.50, 10.50, 6.50),
                    Block.box(3.30, 10.50, 3.30, 4.70, 12.00, 4.70),
                    Block.box(3.50, 12.40, 1.70, 4.50, 13.00, 3.00),
                    Block.box(2.70, 12.00, 2.70, 5.30, 13.20, 5.30),
                    Block.box(9.00, 0.00, 1.00, 15.00, 10.00, 7.00),
                    Block.box(9.50, 10.00, 1.50, 14.50, 10.50, 6.50),
                    Block.box(11.30, 10.50, 3.30, 12.70, 12.00, 4.70),
                    Block.box(11.50, 12.40, 1.70, 12.50, 13.00, 3.00),
                    Block.box(10.70, 12.00, 2.70, 13.30, 13.20, 5.30)),
    };

    /** shampoo.geo.json 北向 + gecko X 镜像 */
    static final VoxelShape SHAMPOO_SINGLE_NORTH =
            StackLayerCollisions.orParts(
                    Block.box(5.00, 0.00, 4.00, 11.00, 8.00, 10.00),
                    Block.box(5.50, 8.00, 4.50, 10.50, 8.50, 9.50),
                    Block.box(7.30, 8.50, 6.30, 8.70, 10.00, 7.70),
                    Block.box(6.70, 10.00, 5.70, 9.30, 11.20, 8.30),
                    Block.box(7.50, 10.40, 4.70, 8.50, 11.00, 6.00));

    /** 层数 1…4 预合并北向碰撞 + gecko X 镜像；下标 layers-1 */
    private static final VoxelShape[] SHAMPOO_NORTH_BY_COUNT = {
            // 1 层
                        StackLayerCollisions.orParts(
                    Block.box(5.00, 0.00, 4.00, 11.00, 8.00, 10.00),
                    Block.box(5.50, 8.00, 4.50, 10.50, 8.50, 9.50),
                    Block.box(7.30, 8.50, 6.30, 8.70, 10.00, 7.70),
                    Block.box(6.70, 10.00, 5.70, 9.30, 11.20, 8.30),
                    Block.box(7.50, 10.40, 4.70, 8.50, 11.00, 6.00)),
            // 2 层
                        StackLayerCollisions.orParts(
                    Block.box(9.00, 0.00, 9.00, 15.00, 8.00, 15.00),
                    Block.box(9.50, 8.00, 9.50, 14.50, 8.50, 14.50),
                    Block.box(11.30, 8.50, 11.30, 12.70, 10.00, 12.70),
                    Block.box(11.50, 10.40, 9.70, 12.50, 11.00, 11.00),
                    Block.box(10.70, 10.00, 10.70, 13.30, 11.20, 13.30),
                    Block.box(1.00, 0.00, 9.00, 7.00, 8.00, 15.00),
                    Block.box(1.50, 8.00, 9.50, 6.50, 8.50, 14.50),
                    Block.box(3.30, 8.50, 11.30, 4.70, 10.00, 12.70),
                    Block.box(3.50, 10.40, 9.70, 4.50, 11.00, 11.00),
                    Block.box(2.70, 10.00, 10.70, 5.30, 11.20, 13.30)),
            // 3 层
                        StackLayerCollisions.orParts(
                    Block.box(9.00, 0.00, 9.00, 15.00, 8.00, 15.00),
                    Block.box(9.50, 8.00, 9.50, 14.50, 8.50, 14.50),
                    Block.box(11.30, 8.50, 11.30, 12.70, 10.00, 12.70),
                    Block.box(11.50, 10.40, 9.70, 12.50, 11.00, 11.00),
                    Block.box(10.70, 10.00, 10.70, 13.30, 11.20, 13.30),
                    Block.box(1.00, 0.00, 9.00, 7.00, 8.00, 15.00),
                    Block.box(1.50, 8.00, 9.50, 6.50, 8.50, 14.50),
                    Block.box(3.30, 8.50, 11.30, 4.70, 10.00, 12.70),
                    Block.box(3.50, 10.40, 9.70, 4.50, 11.00, 11.00),
                    Block.box(2.70, 10.00, 10.70, 5.30, 11.20, 13.30),
                    Block.box(9.00, 0.00, 1.00, 15.00, 8.00, 7.00),
                    Block.box(9.50, 8.00, 1.50, 14.50, 8.50, 6.50),
                    Block.box(11.30, 8.50, 3.30, 12.70, 10.00, 4.70),
                    Block.box(11.50, 10.40, 1.70, 12.50, 11.00, 3.00),
                    Block.box(10.70, 10.00, 2.70, 13.30, 11.20, 5.30)),
            // 4 层
                        StackLayerCollisions.orParts(
                    Block.box(9.00, 0.00, 9.00, 15.00, 8.00, 15.00),
                    Block.box(9.50, 8.00, 9.50, 14.50, 8.50, 14.50),
                    Block.box(11.30, 8.50, 11.30, 12.70, 10.00, 12.70),
                    Block.box(11.50, 10.40, 9.70, 12.50, 11.00, 11.00),
                    Block.box(10.70, 10.00, 10.70, 13.30, 11.20, 13.30),
                    Block.box(1.00, 0.00, 9.00, 7.00, 8.00, 15.00),
                    Block.box(1.50, 8.00, 9.50, 6.50, 8.50, 14.50),
                    Block.box(3.30, 8.50, 11.30, 4.70, 10.00, 12.70),
                    Block.box(3.50, 10.40, 9.70, 4.50, 11.00, 11.00),
                    Block.box(2.70, 10.00, 10.70, 5.30, 11.20, 13.30),
                    Block.box(9.00, 0.00, 1.00, 15.00, 8.00, 7.00),
                    Block.box(9.50, 8.00, 1.50, 14.50, 8.50, 6.50),
                    Block.box(11.30, 8.50, 3.30, 12.70, 10.00, 4.70),
                    Block.box(11.50, 10.40, 1.70, 12.50, 11.00, 3.00),
                    Block.box(10.70, 10.00, 2.70, 13.30, 11.20, 5.30),
                    Block.box(1.00, 0.00, 1.00, 7.00, 8.00, 7.00),
                    Block.box(1.50, 8.00, 1.50, 6.50, 8.50, 6.50),
                    Block.box(3.30, 8.50, 3.30, 4.70, 10.00, 4.70),
                    Block.box(3.50, 10.40, 1.70, 4.50, 11.00, 3.00),
                    Block.box(2.70, 10.00, 2.70, 5.30, 11.20, 5.30)),
    };

    /** soap_paper_box.geo.json 北向 + gecko X 镜像 */
    static final VoxelShape SOAP_PAPER_BOX_SINGLE_NORTH =
            StackLayerCollisions.orParts(
                    Block.box(4.50, 0.00, 6.00, 6.50, 2.20, 10.00),
                    Block.box(6.50, 0.00, 6.00, 11.50, 2.20, 10.00));

    /** 层数 1…7 预合并北向碰撞 + gecko X 镜像；下标 layers-1 */
    private static final VoxelShape[] SOAP_PAPER_BOX_STYLE1_NORTH_BY_COUNT = {
            // 1 层
                        StackLayerCollisions.orParts(
                    Block.box(4.50, 0.00, 6.00, 6.50, 2.20, 10.00),
                    Block.box(6.50, 0.00, 6.00, 11.50, 2.20, 10.00)),
            // 2 层
                        StackLayerCollisions.orParts(
                    Block.box(4.20, 0.00, 6.00, 6.20, 2.20, 10.00),
                    Block.box(6.20, 0.00, 6.00, 11.20, 2.20, 10.00),
                    Block.box(3.56, 2.20, 7.17, 7.80, 4.40, 11.41),
                    Block.box(4.97, 2.20, 3.64, 11.34, 4.40, 10.00)),
            // 3 层
                        StackLayerCollisions.orParts(
                    Block.box(4.20, 0.00, 6.00, 6.20, 2.20, 10.00),
                    Block.box(6.20, 0.00, 6.00, 11.20, 2.20, 10.00),
                    Block.box(3.56, 2.20, 7.17, 7.80, 4.40, 11.41),
                    Block.box(4.97, 2.20, 3.64, 11.34, 4.40, 10.00),
                    Block.box(2.95, 4.40, 5.54, 6.33, 6.60, 10.00),
                    Block.box(4.80, 4.40, 6.30, 10.95, 6.60, 11.91)),
            // 4 层
                        StackLayerCollisions.orParts(
                    Block.box(4.20, 0.00, 6.00, 6.20, 2.20, 10.00),
                    Block.box(6.20, 0.00, 6.00, 11.20, 2.20, 10.00),
                    Block.box(3.56, 2.20, 7.17, 7.80, 4.40, 11.41),
                    Block.box(4.97, 2.20, 3.64, 11.34, 4.40, 10.00),
                    Block.box(2.95, 4.40, 5.54, 6.33, 6.60, 10.00),
                    Block.box(4.80, 4.40, 6.30, 10.95, 6.60, 11.91),
                    Block.box(6.50, 6.60, 5.09, 10.74, 8.80, 9.33),
                    Block.box(2.96, 6.60, 6.50, 9.33, 8.80, 12.86)),
            // 5 层
                        StackLayerCollisions.orParts(
                    Block.box(4.20, 0.00, 6.00, 6.20, 2.20, 10.00),
                    Block.box(6.20, 0.00, 6.00, 11.20, 2.20, 10.00),
                    Block.box(3.56, 2.20, 7.17, 7.80, 4.40, 11.41),
                    Block.box(4.97, 2.20, 3.64, 11.34, 4.40, 10.00),
                    Block.box(2.95, 4.40, 5.54, 6.33, 6.60, 10.00),
                    Block.box(4.80, 4.40, 6.30, 10.95, 6.60, 11.91),
                    Block.box(6.50, 6.60, 5.09, 10.74, 8.80, 9.33),
                    Block.box(2.96, 6.60, 6.50, 9.33, 8.80, 12.86),
                    Block.box(4.80, 8.80, 4.50, 8.80, 11.00, 6.50),
                    Block.box(4.80, 8.80, 6.50, 8.80, 11.00, 11.50)),
            // 6 层
                        StackLayerCollisions.orParts(
                    Block.box(4.20, 0.00, 6.00, 6.20, 2.20, 10.00),
                    Block.box(6.20, 0.00, 6.00, 11.20, 2.20, 10.00),
                    Block.box(3.56, 2.20, 7.17, 7.80, 4.40, 11.41),
                    Block.box(4.97, 2.20, 3.64, 11.34, 4.40, 10.00),
                    Block.box(2.95, 4.40, 5.54, 6.33, 6.60, 10.00),
                    Block.box(4.80, 4.40, 6.30, 10.95, 6.60, 11.91),
                    Block.box(6.50, 6.60, 5.09, 10.74, 8.80, 9.33),
                    Block.box(2.96, 6.60, 6.50, 9.33, 8.80, 12.86),
                    Block.box(4.80, 8.80, 4.50, 8.80, 11.00, 6.50),
                    Block.box(4.80, 8.80, 6.50, 8.80, 11.00, 11.50),
                    Block.box(7.60, 11.00, 4.83, 10.98, 13.20, 9.30),
                    Block.box(2.98, 11.00, 5.60, 9.13, 13.20, 11.21)),
            // 7 层
                        StackLayerCollisions.orParts(
                    Block.box(4.20, 0.00, 6.00, 6.20, 2.20, 10.00),
                    Block.box(6.20, 0.00, 6.00, 11.20, 2.20, 10.00),
                    Block.box(3.56, 2.20, 7.17, 7.80, 4.40, 11.41),
                    Block.box(4.97, 2.20, 3.64, 11.34, 4.40, 10.00),
                    Block.box(2.95, 4.40, 5.54, 6.33, 6.60, 10.00),
                    Block.box(4.80, 4.40, 6.30, 10.95, 6.60, 11.91),
                    Block.box(6.50, 6.60, 5.09, 10.74, 8.80, 9.33),
                    Block.box(2.96, 6.60, 6.50, 9.33, 8.80, 12.86),
                    Block.box(4.80, 8.80, 4.50, 8.80, 11.00, 6.50),
                    Block.box(4.80, 8.80, 6.50, 8.80, 11.00, 11.50),
                    Block.box(7.60, 11.00, 4.83, 10.98, 13.20, 9.30),
                    Block.box(2.98, 11.00, 5.60, 9.13, 13.20, 11.21),
                    Block.box(2.89, 13.20, 3.96, 7.13, 15.40, 8.20),
                    Block.box(4.30, 13.20, 5.37, 10.66, 15.40, 11.74)),
    };

    /** 层数 1…7 预合并北向碰撞 + gecko X 镜像；下标 layers-1 */
    private static final VoxelShape[] SOAP_PAPER_BOX_STYLE2_NORTH_BY_COUNT = {
            // 1 层
                        StackLayerCollisions.orParts(
                    Block.box(4.50, 0.00, 6.00, 6.50, 2.20, 10.00),
                    Block.box(6.50, 0.00, 6.00, 11.50, 2.20, 10.00)),
            // 2 层
                        StackLayerCollisions.orParts(
                    Block.box(12.00, 0.00, 10.00, 16.00, 2.20, 12.00),
                    Block.box(12.00, 0.00, 5.00, 16.00, 2.20, 10.00),
                    Block.box(6.00, 0.00, 10.00, 10.00, 2.20, 12.00),
                    Block.box(6.00, 0.00, 5.00, 10.00, 2.20, 10.00)),
            // 3 层
                        StackLayerCollisions.orParts(
                    Block.box(12.00, 0.00, 10.00, 16.00, 2.20, 12.00),
                    Block.box(12.00, 0.00, 5.00, 16.00, 2.20, 10.00),
                    Block.box(6.00, 0.00, 10.00, 10.00, 2.20, 12.00),
                    Block.box(6.00, 0.00, 5.00, 10.00, 2.20, 10.00),
                    Block.box(9.00, 2.20, 5.00, 13.00, 4.40, 7.00),
                    Block.box(9.00, 2.20, 7.00, 13.00, 4.40, 12.00)),
            // 4 层
                        StackLayerCollisions.orParts(
                    Block.box(12.00, 0.00, 10.00, 16.00, 2.20, 12.00),
                    Block.box(12.00, 0.00, 5.00, 16.00, 2.20, 10.00),
                    Block.box(6.00, 0.00, 10.00, 10.00, 2.20, 12.00),
                    Block.box(6.00, 0.00, 5.00, 10.00, 2.20, 10.00),
                    Block.box(9.00, 2.20, 5.00, 13.00, 4.40, 7.00),
                    Block.box(9.00, 2.20, 7.00, 13.00, 4.40, 12.00),
                    Block.box(0.00, 0.00, 10.00, 4.00, 2.20, 12.00),
                    Block.box(0.00, 0.00, 5.00, 4.00, 2.20, 10.00)),
            // 5 层
                        StackLayerCollisions.orParts(
                    Block.box(12.00, 0.00, 10.00, 16.00, 2.20, 12.00),
                    Block.box(12.00, 0.00, 5.00, 16.00, 2.20, 10.00),
                    Block.box(6.00, 0.00, 10.00, 10.00, 2.20, 12.00),
                    Block.box(6.00, 0.00, 5.00, 10.00, 2.20, 10.00),
                    Block.box(9.00, 2.20, 5.00, 13.00, 4.40, 7.00),
                    Block.box(9.00, 2.20, 7.00, 13.00, 4.40, 12.00),
                    Block.box(0.00, 0.00, 10.00, 4.00, 2.20, 12.00),
                    Block.box(0.00, 0.00, 5.00, 4.00, 2.20, 10.00),
                    Block.box(3.00, 2.20, 10.00, 7.00, 4.40, 12.00),
                    Block.box(3.00, 2.20, 5.00, 7.00, 4.40, 10.00)),
            // 6 层
                        StackLayerCollisions.orParts(
                    Block.box(12.00, 0.00, 10.00, 16.00, 2.20, 12.00),
                    Block.box(12.00, 0.00, 5.00, 16.00, 2.20, 10.00),
                    Block.box(6.00, 0.00, 10.00, 10.00, 2.20, 12.00),
                    Block.box(6.00, 0.00, 5.00, 10.00, 2.20, 10.00),
                    Block.box(9.00, 2.20, 5.00, 13.00, 4.40, 7.00),
                    Block.box(9.00, 2.20, 7.00, 13.00, 4.40, 12.00),
                    Block.box(0.00, 0.00, 10.00, 4.00, 2.20, 12.00),
                    Block.box(0.00, 0.00, 5.00, 4.00, 2.20, 10.00),
                    Block.box(3.00, 2.20, 10.00, 7.00, 4.40, 12.00),
                    Block.box(3.00, 2.20, 5.00, 7.00, 4.40, 10.00),
                    Block.box(4.50, 4.40, 6.50, 6.50, 6.60, 10.50),
                    Block.box(6.50, 4.40, 6.50, 11.50, 6.60, 10.50)),
            // 7 层
                        StackLayerCollisions.orParts(
                    Block.box(12.00, 0.00, 10.00, 16.00, 2.20, 12.00),
                    Block.box(12.00, 0.00, 5.00, 16.00, 2.20, 10.00),
                    Block.box(6.00, 0.00, 10.00, 10.00, 2.20, 12.00),
                    Block.box(6.00, 0.00, 5.00, 10.00, 2.20, 10.00),
                    Block.box(9.00, 2.20, 5.00, 13.00, 4.40, 7.00),
                    Block.box(9.00, 2.20, 7.00, 13.00, 4.40, 12.00),
                    Block.box(0.00, 0.00, 10.00, 4.00, 2.20, 12.00),
                    Block.box(0.00, 0.00, 5.00, 4.00, 2.20, 10.00),
                    Block.box(3.00, 2.20, 10.00, 7.00, 4.40, 12.00),
                    Block.box(3.00, 2.20, 5.00, 7.00, 4.40, 10.00),
                    Block.box(4.50, 4.40, 6.50, 6.50, 6.60, 10.50),
                    Block.box(6.50, 4.40, 6.50, 11.50, 6.60, 10.50),
                    Block.box(9.50, 6.60, 8.00, 11.50, 10.60, 10.20),
                    Block.box(4.50, 6.60, 8.00, 9.50, 10.60, 10.20)),
    };

    public static VoxelShape soapPaperBagNorth(int layers) {
        return northByCount(SOAP_PAPER_BAG_NORTH_BY_COUNT, layers);
    }

    public static VoxelShape bodyCreamNorth(int layers) {
        return northByCount(BODY_CREAM_NORTH_BY_COUNT, layers);
    }

    public static VoxelShape bodyWashNorth(int layers) {
        return northByCount(BODY_WASH_NORTH_BY_COUNT, layers);
    }

    public static VoxelShape shampooNorth(int layers) {
        return northByCount(SHAMPOO_NORTH_BY_COUNT, layers);
    }

    public static VoxelShape soapPaperBoxNorth(int layers, int stackStyle) {
        VoxelShape[] table =
                stackStyle == 2 ? SOAP_PAPER_BOX_STYLE2_NORTH_BY_COUNT : SOAP_PAPER_BOX_STYLE1_NORTH_BY_COUNT;
        return northByCount(table, layers);
    }
}
