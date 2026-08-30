package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.bed.BedPlateSide;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 床板1型北向分格碰撞（相对<strong>床尾右</strong>渲染锚点 + Gecko X 镜像后裁切）。
 *
 * <p>由 {@code bed_plate1.geo.json} 各 cube 映射裁切；含左侧长墙、床头墙、床尾半墙与床垫。
 * 全结构 9 盒（≤10）。朝向用 {@link VoxelShapeRotation#rotateYFromNorthLikeGeckoBlockRenderer}。
 */
final class BedPlate1CollisionShapes {

    private BedPlate1CollisionShapes() {}

    /** 床尾右：仅床垫。 */
    private static final VoxelShape FOOT_RIGHT =
            Block.box(0.00, 0.00, 0.00, 16.00, 4.00, 14.00);

    /** 床尾左：西侧长墙 + 床尾半墙 + 床垫。 */
    private static final VoxelShape FOOT_LEFT =
            Shapes.or(
                    Block.box(0.00, 0.00, 0.00, 2.00, 16.00, 16.00),
                    Block.box(2.00, 0.00, 14.00, 16.00, 16.00, 16.00),
                    Block.box(2.00, 0.00, 0.00, 16.00, 4.00, 14.00));

    /** 床头右：床头墙 + 床垫。 */
    private static final VoxelShape HEAD_RIGHT =
            Shapes.or(
                    Block.box(0.00, 0.00, 0.00, 16.00, 16.00, 2.00),
                    Block.box(0.00, 0.00, 2.00, 16.00, 4.00, 16.00));

    /** 床头左：西侧长墙 + 床头墙 + 床垫。 */
    private static final VoxelShape HEAD_LEFT =
            Shapes.or(
                    Block.box(0.00, 0.00, 0.00, 2.00, 16.00, 16.00),
                    Block.box(2.00, 0.00, 0.00, 16.00, 16.00, 2.00),
                    Block.box(2.00, 0.00, 2.00, 16.00, 4.00, 16.00));

    static VoxelShape shapeFor(BlockState state) {
        VoxelShape north = northFor(state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE));
        Direction facing = state.getValue(BedPlate1Block.FACING);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }

    private static VoxelShape northFor(BedPart part, BedPlateSide side) {
        if (part == BedPart.FOOT) {
            return side == BedPlateSide.LEFT ? FOOT_LEFT : FOOT_RIGHT;
        }
        return side == BedPlateSide.LEFT ? HEAD_LEFT : HEAD_RIGHT;
    }
}
