package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.bed.BedPlateSide;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 床板1型北向分格碰撞 / 选取（相对<strong>床尾右</strong>渲染锚点 + Gecko X 镜像后裁切）。
 *
 * <p>有床单 / 被套时 {@link #pickShapeFor} 并入叠层供射线命中；准心按 {@link #pickLayer} 优先被套 → 床单 →
 * 床体。
 */
public final class BedPlate1CollisionShapes {

    public enum PickedLayer {
        BODY,
        DUVET,
        DUVET_COVER
    }

    private static final double PICK_BOUNDARY_EPS = 1.0E-3;

    private BedPlate1CollisionShapes() {}

    private static final VoxelShape FOOT_RIGHT =
            Block.box(0.00, 0.00, 0.00, 16.00, 4.00, 14.00);

    private static final VoxelShape FOOT_LEFT =
            Shapes.or(
                    Block.box(0.00, 0.00, 0.00, 2.00, 16.00, 16.00),
                    Block.box(2.00, 0.00, 14.00, 16.00, 16.00, 16.00),
                    Block.box(2.00, 0.00, 0.00, 16.00, 4.00, 14.00));

    private static final VoxelShape HEAD_RIGHT =
            Shapes.or(
                    Block.box(0.00, 0.00, 0.00, 16.00, 16.00, 2.00),
                    Block.box(0.00, 0.00, 2.00, 16.00, 4.00, 16.00));

    private static final VoxelShape HEAD_LEFT =
            Shapes.or(
                    Block.box(0.00, 0.00, 0.00, 2.00, 16.00, 16.00),
                    Block.box(2.00, 0.00, 0.00, 16.00, 16.00, 2.00),
                    Block.box(2.00, 0.00, 2.00, 16.00, 4.00, 16.00));

    /** 床单：{@code bed_plate1_duvet} gecko 裁切。 */
    private static final VoxelShape DUVET_FOOT_RIGHT =
            Block.box(0.00, 4.00, 0.00, 16.00, 7.00, 14.00);
    private static final VoxelShape DUVET_FOOT_LEFT =
            Block.box(2.00, 4.00, 0.00, 16.00, 7.00, 14.00);
    private static final VoxelShape DUVET_HEAD_RIGHT =
            Block.box(0.00, 4.00, 2.00, 16.00, 7.00, 16.00);
    private static final VoxelShape DUVET_HEAD_LEFT =
            Block.box(2.00, 4.00, 2.00, 16.00, 7.00, 16.00);

    /**
     * 被套：{@code bed_plate1_duvet_cover} gecko raw (−22.13,6.54,−15)–(9.53,13.39,6) 分格裁切。
     */
    private static final VoxelShape COVER_FOOT_RIGHT =
            Block.box(0.00, 6.50, 0.00, 9.53, 13.40, 6.00);
    private static final VoxelShape COVER_FOOT_LEFT =
            Block.box(0.00, 6.50, 0.00, 16.00, 13.40, 6.00);
    private static final VoxelShape COVER_HEAD_RIGHT =
            Block.box(0.00, 6.50, 1.00, 9.53, 13.40, 16.00);
    private static final VoxelShape COVER_HEAD_LEFT =
            Block.box(0.00, 6.50, 1.00, 16.00, 13.40, 16.00);

    public static VoxelShape bodyShape(BlockState state) {
        return orient(northBody(state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE)), state);
    }

    public static VoxelShape duvetShape(BlockState state) {
        return orient(
                northDuvet(state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE)), state);
    }

    public static VoxelShape coverShape(BlockState state) {
        return orient(
                northCover(state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE)), state);
    }

    public static VoxelShape pickShapeFor(BlockState state, boolean hasDuvet, boolean hasCover) {
        VoxelShape s = bodyShape(state);
        if (hasDuvet) {
            s = Shapes.or(s, duvetShape(state));
        }
        if (hasCover) {
            s = Shapes.or(s, coverShape(state));
        }
        return s;
    }

    /** 被套 &gt; 床单 &gt; 床体。 */
    public static PickedLayer pickLayer(
            BlockState state, boolean hasDuvet, boolean hasCover, Vec3 hitWorld, BlockPos pos) {
        if (hasCover && containsLocal(coverShape(state), hitWorld, pos)) {
            return PickedLayer.DUVET_COVER;
        }
        if (hasDuvet && containsLocal(duvetShape(state), hitWorld, pos)) {
            return PickedLayer.DUVET;
        }
        return PickedLayer.BODY;
    }

    public static VoxelShape outlineShape(
            BlockState state, boolean hasDuvet, boolean hasCover, PickedLayer layer) {
        return switch (layer) {
            case DUVET_COVER -> hasCover ? coverShape(state) : bodyShape(state);
            case DUVET -> hasDuvet ? duvetShape(state) : bodyShape(state);
            case BODY -> bodyShape(state);
        };
    }

    private static boolean containsLocal(VoxelShape orientedShape, Vec3 hitWorld, BlockPos pos) {
        double px = hitWorld.x - pos.getX();
        double py = hitWorld.y - pos.getY();
        double pz = hitWorld.z - pos.getZ();
        for (AABB box : orientedShape.toAabbs()) {
            if (box.inflate(PICK_BOUNDARY_EPS).contains(px, py, pz)) {
                return true;
            }
        }
        return false;
    }

    private static VoxelShape orient(VoxelShape north, BlockState state) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                north, state.getValue(BedPlate1Block.FACING));
    }

    private static VoxelShape northBody(BedPart part, BedPlateSide side) {
        if (part == BedPart.FOOT) {
            return side == BedPlateSide.LEFT ? FOOT_LEFT : FOOT_RIGHT;
        }
        return side == BedPlateSide.LEFT ? HEAD_LEFT : HEAD_RIGHT;
    }

    private static VoxelShape northDuvet(BedPart part, BedPlateSide side) {
        if (part == BedPart.FOOT) {
            return side == BedPlateSide.LEFT ? DUVET_FOOT_LEFT : DUVET_FOOT_RIGHT;
        }
        return side == BedPlateSide.LEFT ? DUVET_HEAD_LEFT : DUVET_HEAD_RIGHT;
    }

    private static VoxelShape northCover(BedPart part, BedPlateSide side) {
        if (part == BedPart.FOOT) {
            return side == BedPlateSide.LEFT ? COVER_FOOT_LEFT : COVER_FOOT_RIGHT;
        }
        return side == BedPlateSide.LEFT ? COVER_HEAD_LEFT : COVER_HEAD_RIGHT;
    }
}
