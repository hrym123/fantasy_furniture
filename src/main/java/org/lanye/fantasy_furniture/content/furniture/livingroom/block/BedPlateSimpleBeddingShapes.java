package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 床板2/3/4（1×2）：独立床单 / 被套叠层的北向选取盒（自对应 duvet geo · --gecko-block X）。
 *
 * <p>有寝具时并入 {@link #pickShapeFor} 供射线命中；准心层序：被套 → 床单 → 床体。
 */
public final class BedPlateSimpleBeddingShapes {

    public enum Plate {
        PLATE2,
        PLATE3,
        PLATE4
    }

    public enum PickedLayer {
        BODY,
        DUVET,
        DUVET_COVER
    }

    private static final double PICK_BOUNDARY_EPS = 1.0E-3;

    private BedPlateSimpleBeddingShapes() {}

    /* 板2 床单：geo bed_plate2_duvet · FOOT AABB；HEAD 按床垫延伸 */
    private static final VoxelShape P2_DUVET_FOOT = Block.box(1.00, 5.00, 0.00, 16.00, 9.00, 15.00);
    private static final VoxelShape P2_DUVET_HEAD = Block.box(1.00, 5.00, 1.00, 16.00, 9.00, 16.00);
    private static final VoxelShape P2_COVER_FOOT = Block.box(1.00, 5.37, 0.00, 16.00, 11.00, 15.00);
    private static final VoxelShape P2_COVER_HEAD = Block.box(1.00, 5.37, 1.00, 16.00, 11.00, 16.00);

    /* 板3 */
    private static final VoxelShape P3_DUVET_FOOT = Block.box(0.50, 3.00, 0.00, 15.50, 4.50, 14.00);
    private static final VoxelShape P3_DUVET_HEAD = Block.box(0.50, 3.00, 2.00, 15.50, 4.50, 16.00);
    private static final VoxelShape P3_COVER_FOOT = Block.box(0.00, 2.00, 0.00, 16.00, 10.11, 14.00);
    private static final VoxelShape P3_COVER_HEAD = Block.box(0.00, 2.00, 2.00, 16.00, 10.11, 16.00);

    /* 板4 */
    private static final VoxelShape P4_DUVET_FOOT = Block.box(0.50, 3.00, 0.00, 15.50, 5.00, 15.50);
    private static final VoxelShape P4_DUVET_HEAD = Block.box(0.50, 3.00, 1.00, 15.50, 5.00, 16.00);
    private static final VoxelShape P4_COVER_FOOT = Block.box(0.00, 0.70, 0.00, 16.00, 9.27, 16.00);
    private static final VoxelShape P4_COVER_HEAD = Block.box(0.00, 0.70, 0.00, 16.00, 9.27, 16.00);

    public static VoxelShape bodyShape(Plate plate, BlockState state) {
        return switch (plate) {
            case PLATE2 -> BedPlateEmptyBedCollision.shapeFor(
                    state, BedPlateEmptyBedCollision.PLATE2_FOOT, BedPlateEmptyBedCollision.PLATE2_HEAD);
            case PLATE3 -> BedPlateEmptyBedCollision.shapeFor(
                    state, BedPlateEmptyBedCollision.PLATE3_FOOT, BedPlateEmptyBedCollision.PLATE3_HEAD);
            case PLATE4 -> BedPlateEmptyBedCollision.shapeFor(
                    state, BedPlateEmptyBedCollision.PLATE4_FOOT, BedPlateEmptyBedCollision.PLATE4_HEAD);
        };
    }

    public static VoxelShape duvetShape(Plate plate, BlockState state) {
        return orient(northDuvet(plate, state.getValue(BedBlock.PART)), state);
    }

    public static VoxelShape coverShape(Plate plate, BlockState state) {
        return orient(northCover(plate, state.getValue(BedBlock.PART)), state);
    }

    public static VoxelShape pickShapeFor(
            Plate plate, BlockState state, boolean hasDuvet, boolean hasCover) {
        VoxelShape s = bodyShape(plate, state);
        if (hasDuvet) {
            s = Shapes.or(s, duvetShape(plate, state));
        }
        if (hasCover) {
            s = Shapes.or(s, coverShape(plate, state));
        }
        return s;
    }

    /** 被套 &gt; 床单 &gt; 床体。 */
    public static PickedLayer pickLayer(
            Plate plate,
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            Vec3 hitWorld,
            BlockPos pos) {
        if (hasCover && containsLocal(coverShape(plate, state), hitWorld, pos)) {
            return PickedLayer.DUVET_COVER;
        }
        if (hasDuvet && containsLocal(duvetShape(plate, state), hitWorld, pos)) {
            return PickedLayer.DUVET;
        }
        return PickedLayer.BODY;
    }

    public static VoxelShape outlineShape(
            Plate plate, BlockState state, boolean hasDuvet, boolean hasCover, PickedLayer layer) {
        return switch (layer) {
            case DUVET_COVER -> hasCover ? coverShape(plate, state) : bodyShape(plate, state);
            case DUVET -> hasDuvet ? duvetShape(plate, state) : bodyShape(plate, state);
            case BODY -> bodyShape(plate, state);
        };
    }

    private static VoxelShape northDuvet(Plate plate, BedPart part) {
        boolean foot = part == BedPart.FOOT;
        return switch (plate) {
            case PLATE2 -> foot ? P2_DUVET_FOOT : P2_DUVET_HEAD;
            case PLATE3 -> foot ? P3_DUVET_FOOT : P3_DUVET_HEAD;
            case PLATE4 -> foot ? P4_DUVET_FOOT : P4_DUVET_HEAD;
        };
    }

    private static VoxelShape northCover(Plate plate, BedPart part) {
        boolean foot = part == BedPart.FOOT;
        return switch (plate) {
            case PLATE2 -> foot ? P2_COVER_FOOT : P2_COVER_HEAD;
            case PLATE3 -> foot ? P3_COVER_FOOT : P3_COVER_HEAD;
            case PLATE4 -> foot ? P4_COVER_FOOT : P4_COVER_HEAD;
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
                north, state.getValue(BedBlock.FACING));
    }
}
