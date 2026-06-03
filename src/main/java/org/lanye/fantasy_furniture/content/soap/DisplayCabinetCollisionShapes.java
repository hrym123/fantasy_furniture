package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 陈列柜北向碰撞（源自 geo；打开态排除 {@code door} 骨骼）。 */
public final class DisplayCabinetCollisionShapes {

    /** 关盒：{@code display_cabinet.geo.json} 全量并集。 */
    public static final VoxelShape NORTH_CLOSED = buildClosedNorthUnion();

    /** 打开：{@code display_cabinet_open.geo.json} 并集，不含 {@code door}。 */
    public static final VoxelShape NORTH_OPEN = buildOpenNorthUnion();

    private DisplayCabinetCollisionShapes() {}

    public static VoxelShape northForOpen(boolean open) {
        return open ? NORTH_OPEN : NORTH_CLOSED;
    }

    private static VoxelShape buildClosedNorthUnion() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
        shape = Shapes.or(shape, Block.box(0.0, 1.0, 8.0, 1.0, 15.0, 15.0));
        shape = Shapes.or(shape, Block.box(15.0, 1.0, 8.0, 16.0, 15.0, 15.0));
        shape = Shapes.or(shape, Block.box(0.0, 0.0, 8.0, 16.0, 1.0, 15.0));
        shape = Shapes.or(shape, Block.box(0.0, 15.0, 8.0, 16.0, 16.0, 15.0));
        shape = Shapes.or(shape, Block.box(1.0, 0.0, 7.0, 15.0, 1.0, 8.0));
        shape = Shapes.or(shape, Block.box(1.0, 15.0, 7.0, 15.0, 16.0, 8.0));
        shape = Shapes.or(shape, Block.box(15.0, 0.0, 7.0, 16.0, 16.0, 8.0));
        shape = Shapes.or(shape, Block.box(0.0, 0.0, 7.0, 1.0, 16.0, 8.0));
        shape = Shapes.or(shape, Block.box(1.0, 1.0, 7.2, 15.0, 15.0, 7.8));
        shape = Shapes.or(shape, Block.box(11.0, 0.1, 6.0, 12.0, 0.9, 7.0));
        shape = Shapes.or(shape, Block.box(5.0, 0.1, 6.0, 11.0, 0.9, 6.5));
        shape = Shapes.or(shape, Block.box(4.0, 0.1, 6.0, 5.0, 0.9, 7.0));
        return shape;
    }

    private static VoxelShape buildOpenNorthUnion() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
        shape = Shapes.or(shape, Block.box(0.0, 1.0, 8.0, 1.0, 15.0, 15.0));
        shape = Shapes.or(shape, Block.box(15.0, 1.0, 8.0, 16.0, 15.0, 15.0));
        shape = Shapes.or(shape, Block.box(0.0, 0.0, 8.0, 16.0, 1.0, 15.0));
        shape = Shapes.or(shape, Block.box(0.0, 15.0, 8.0, 16.0, 16.0, 15.0));
        return shape;
    }
}
