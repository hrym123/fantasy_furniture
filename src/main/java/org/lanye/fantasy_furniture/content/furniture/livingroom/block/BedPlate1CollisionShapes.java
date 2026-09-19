package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.reverie_core.geolib.bed.BedPlateSide;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 床板1型北向分格碰撞 / 选取（相对<strong>床尾右</strong>渲染锚点 + Gecko X 镜像后裁切）。
 *
 * <p>准心层序：小号枕 → 中号枕 → 大号枕 → 被套 → 床单 → 床体。枕头体素来自组合 geo，不另加厚。
 */
public final class BedPlate1CollisionShapes {

    public enum PickedLayer {
        BODY,
        DUVET,
        DUVET_COVER,
        LARGE_1,
        LARGE_2,
        MEDIUM,
        SMALL
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

    public static VoxelShape bodyShape(BlockState state) {
        return orient(northBody(state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE)), state);
    }

    public static VoxelShape duvetShape(BlockState state) {
        return orient(
                northDuvet(state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE)), state);
    }

    public static VoxelShape coverShape(BlockState state) {
        return orient(
                clipCells(
                        BedPlateCoverPickShapes.plate1(),
                        state.getValue(BedPlate1Block.PART),
                        state.getValue(BedPlate1Block.SIDE)),
                state);
    }

    public static VoxelShape pickShapeFor(
            BlockState state, boolean hasDuvet, boolean hasCover, BedPlateSheetPillowSlots slots) {
        VoxelShape s = bodyShape(state);
        if (hasDuvet) {
            s = Shapes.or(s, duvetShape(state));
        }
        if (hasCover) {
            s = Shapes.or(s, coverShape(state));
        }
        if (slots != null && slots.hasAny()) {
            s = Shapes.or(s, pillowUnion(state, slots));
        }
        return s;
    }

    /** 小号枕 &gt; 中号枕 &gt; 大号枕 &gt; 被套 &gt; 床单 &gt; 床体。 */
    public static PickedLayer pickLayer(
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            BedPlateSheetPillowSlots slots,
            Vec3 hitWorld,
            BlockPos pos) {
        if (slots != null) {
            if (hitPillow(state, slots, PickedLayer.SMALL, hitWorld, pos)) {
                return PickedLayer.SMALL;
            }
            if (hitPillow(state, slots, PickedLayer.MEDIUM, hitWorld, pos)) {
                return PickedLayer.MEDIUM;
            }
            if (hitPillow(state, slots, PickedLayer.LARGE_1, hitWorld, pos)) {
                return PickedLayer.LARGE_1;
            }
            if (hitPillow(state, slots, PickedLayer.LARGE_2, hitWorld, pos)) {
                return PickedLayer.LARGE_2;
            }
        }
        if (hasCover && containsLocal(coverShape(state), hitWorld, pos)) {
            return PickedLayer.DUVET_COVER;
        }
        if (hasDuvet && containsLocal(duvetShape(state), hitWorld, pos)) {
            return PickedLayer.DUVET;
        }
        return PickedLayer.BODY;
    }

    public static VoxelShape outlineShape(
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            BedPlateSheetPillowSlots slots,
            PickedLayer layer) {
        return switch (layer) {
            case SMALL, MEDIUM, LARGE_1, LARGE_2 -> pillowCell(state, slots, layer);
            case DUVET_COVER -> hasCover ? coverShape(state) : bodyShape(state);
            case DUVET -> hasDuvet ? duvetShape(state) : bodyShape(state);
            case BODY -> bodyShape(state);
        };
    }

    private static boolean hitPillow(
            BlockState state,
            BedPlateSheetPillowSlots slots,
            PickedLayer layer,
            Vec3 hitWorld,
            BlockPos pos) {
        VoxelShape cell = pillowCell(state, slots, layer);
        return !cell.isEmpty() && containsLocal(cell, hitWorld, pos);
    }

    private static VoxelShape pillowUnion(BlockState state, BedPlateSheetPillowSlots slots) {
        VoxelShape s = Shapes.empty();
        for (PickedLayer layer :
                new PickedLayer[] {PickedLayer.LARGE_1, PickedLayer.LARGE_2, PickedLayer.MEDIUM, PickedLayer.SMALL}) {
            s = Shapes.or(s, pillowCell(state, slots, layer));
        }
        return s;
    }

    /** 锚点北向体素裁进当前格（0–1），再按朝向转正。 */
    private static VoxelShape pillowCell(BlockState state, BedPlateSheetPillowSlots slots, PickedLayer layer) {
        VoxelShape north = BedPlate1PillowPickShapes.northFor(slots, layer);
        if (north.isEmpty()) {
            return Shapes.empty();
        }
        return orient(
                sliceToCell(north, state.getValue(BedPlate1Block.PART), state.getValue(BedPlate1Block.SIDE)),
                state);
    }

    /** 枕头并集体素已在单盒内，可直接 {@code toAabbs}。 */
    private static VoxelShape sliceToCell(VoxelShape anchorNorth, BedPart part, BedPlateSide side) {
        return clipCells(anchorNorth.toAabbs(), part, side);
    }

    /** 被套逐盒裁切，避免并集丢掉负 z 立板。枕头仍走 {@link #sliceToCell}。 */
    private static VoxelShape clipCells(Iterable<AABB> boxes, BedPart part, BedPlateSide side) {
        boolean head = part == BedPart.HEAD;
        boolean left = side == BedPlateSide.LEFT;
        double x0 = left ? -1.0 : 0.0;
        double z0 = head ? -1.0 : 0.0;
        double x1 = x0 + 1.0;
        double z1 = z0 + 1.0;
        VoxelShape out = Shapes.empty();
        for (AABB box : boxes) {
            double ix0 = Math.max(box.minX, x0);
            double iy0 = Math.max(box.minY, 0.0);
            double iz0 = Math.max(box.minZ, z0);
            double ix1 = Math.min(box.maxX, x1);
            double iy1 = Math.min(box.maxY, 1.0);
            double iz1 = Math.min(box.maxZ, z1);
            if (ix1 - ix0 < 1.0E-4 || iy1 - iy0 < 1.0E-4 || iz1 - iz0 < 1.0E-4) {
                continue;
            }
            out = Shapes.or(out, Shapes.box(ix0 - x0, iy0, iz0 - z0, ix1 - x0, iy1, iz1 - z0));
        }
        return out;
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
}
