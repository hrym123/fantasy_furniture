package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateComboPillowLayout;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import org.lanye.reverie_core.util.VoxelShapeTranslation;

/**
 * 床板2/3/4（1×2）：独立床单 / 被套 / 枕头的北向选取盒。
 *
 * <p>枕头来自该型号组合 geo，与绘制同一组后缀。层序：小号枕 → 中号枕 → 大号枕 → 被套 → 床单 → 床体。
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
        DUVET_COVER,
        LARGE,
        MEDIUM,
        SMALL
    }

    private static final double PICK_BOUNDARY_EPS = 1.0E-3;

    private BedPlateSimpleBeddingShapes() {}

    /* 板2 床单：geo bed_plate2_duvet */
    private static final VoxelShape P2_DUVET_FOOT = Block.box(1.00, 5.00, 0.00, 16.00, 9.00, 15.00);
    private static final VoxelShape P2_DUVET_HEAD = Block.box(1.00, 5.00, 1.00, 16.00, 9.00, 16.00);

    /* 板3 */
    private static final VoxelShape P3_DUVET_FOOT = Block.box(0.50, 3.00, 0.00, 15.50, 4.50, 14.00);
    private static final VoxelShape P3_DUVET_HEAD = Block.box(0.50, 3.00, 2.00, 15.50, 4.50, 16.00);

    /* 板4 */
    private static final VoxelShape P4_DUVET_FOOT = Block.box(0.50, 3.00, 0.00, 15.50, 5.00, 15.50);
    private static final VoxelShape P4_DUVET_HEAD = Block.box(0.50, 3.00, 1.00, 15.50, 5.00, 16.00);

    /** 整条床单，相对床尾（床头半段在 z−1）。 */
    private static final VoxelShape P2_DUVET_WHOLE = wholeDuvet(P2_DUVET_FOOT, P2_DUVET_HEAD);
    private static final VoxelShape P3_DUVET_WHOLE = wholeDuvet(P3_DUVET_FOOT, P3_DUVET_HEAD);
    private static final VoxelShape P4_DUVET_WHOLE = wholeDuvet(P4_DUVET_FOOT, P4_DUVET_HEAD);

    private static final VoxelShape P2_COVER_WHOLE = unionBoxes(BedPlateCoverPickShapes.plate2());
    private static final VoxelShape P3_COVER_WHOLE = unionBoxes(BedPlateCoverPickShapes.plate3());
    private static final VoxelShape P4_COVER_WHOLE = unionBoxes(BedPlateCoverPickShapes.plate4());

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
        List<AABB> north =
                switch (plate) {
                    case PLATE2 -> BedPlateCoverPickShapes.plate2();
                    case PLATE3 -> BedPlateCoverPickShapes.plate3();
                    case PLATE4 -> BedPlateCoverPickShapes.plate4();
                };
        return orient(sliceCover(north, state.getValue(BedBlock.PART)), state);
    }

    public static VoxelShape pickShapeFor(
            Plate plate,
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            int largeStyle,
            int mediumMat,
            int smallMat,
            BedPlateSheetPillowSlots pillows) {
        VoxelShape s = bodyShape(plate, state);
        if (hasDuvet) {
            s = Shapes.or(s, duvetShape(plate, state));
        }
        if (hasCover) {
            s = Shapes.or(s, coverShape(plate, state));
        }
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.LARGE, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.MEDIUM, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.SMALL, pillows));
        return s;
    }

    /** 小号枕 &gt; 中号枕 &gt; 大号枕 &gt; 被套 &gt; 床单 &gt; 床体。 */
    public static PickedLayer pickLayer(
            Plate plate,
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            int largeStyle,
            int mediumMat,
            int smallMat,
            BedPlateSheetPillowSlots pillows,
            Vec3 hitWorld,
            BlockPos pos) {
        if (hitPillow(plate, state, PickedLayer.SMALL, pillows, hitWorld, pos)) {
            return PickedLayer.SMALL;
        }
        if (hitPillow(plate, state, PickedLayer.MEDIUM, pillows, hitWorld, pos)) {
            return PickedLayer.MEDIUM;
        }
        if (hitPillow(plate, state, PickedLayer.LARGE, pillows, hitWorld, pos)) {
            return PickedLayer.LARGE;
        }
        if (hasCover && containsLocal(coverShape(plate, state), hitWorld, pos)) {
            return PickedLayer.DUVET_COVER;
        }
        if (hasDuvet && containsLocal(duvetShape(plate, state), hitWorld, pos)) {
            return PickedLayer.DUVET;
        }
        return PickedLayer.BODY;
    }

    /** 准心打中的是哪一只大号。平放共用 {@code large_p1} 时，按还在的那一槽。 */
    public static int largeSlotAt(
            Plate plate,
            BlockState state,
            BedPlateSheetPillowSlots pillows,
            Vec3 hitWorld,
            BlockPos pos) {
        if (pillows == null) {
            return 0;
        }
        int plateId =
                switch (plate) {
                    case PLATE2 -> 2;
                    case PLATE3 -> 3;
                    case PLATE4 -> 4;
                };
        BedPart part = state.getValue(BedBlock.PART);
        for (String suffix : BedPlateComboPillowLayout.suffixes(plateId, pillows)) {
            if (!suffix.startsWith("large_")) {
                continue;
            }
            VoxelShape cell =
                    orient(sliceCover(BedPlateComboPillowPickShapes.of(plateId, suffix), part), state);
            if (cell.isEmpty() || !containsLocal(cell, hitWorld, pos)) {
                continue;
            }
            if (suffix.endsWith("2")) {
                return 2;
            }
            return pillows.hasLargeSlot(1) ? 1 : 2;
        }
        return 0;
    }

    /** 命中的中号槽。后缀末位是 1/2/3；没有命中返回 0。 */
    public static int mediumSlotAt(
            Plate plate,
            BlockState state,
            BedPlateSheetPillowSlots pillows,
            Vec3 hitWorld,
            BlockPos pos) {
        if (pillows == null) {
            return 0;
        }
        int plateId =
                switch (plate) {
                    case PLATE2 -> 2;
                    case PLATE3 -> 3;
                    case PLATE4 -> 4;
                };
        BedPart part = state.getValue(BedBlock.PART);
        for (String suffix : BedPlateComboPillowLayout.suffixes(plateId, pillows)) {
            if (!suffix.startsWith("medium_")) {
                continue;
            }
            VoxelShape cell =
                    orient(sliceCover(BedPlateComboPillowPickShapes.of(plateId, suffix), part), state);
            if (cell.isEmpty() || !containsLocal(cell, hitWorld, pos)) {
                continue;
            }
            char last = suffix.charAt(suffix.length() - 1);
            return last >= '1' && last <= '3' ? last - '0' : 1;
        }
        return 0;
    }

    /**
     * 组件描边：相对床尾的整件（不裁格、不含木架）。床体返回空，由调用方画命中格。
     */
    public static VoxelShape outlineComponent(
            Plate plate,
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            BedPlateSheetPillowSlots pillows,
            PickedLayer layer) {
        VoxelShape north =
                switch (layer) {
                    case DUVET -> hasDuvet ? duvetWhole(plate) : Shapes.empty();
                    case DUVET_COVER -> hasCover ? coverWhole(plate) : Shapes.empty();
                    case SMALL, MEDIUM, LARGE -> unionBoxes(pillowBoxes(plate, layer, pillows));
                    case BODY -> Shapes.empty();
                };
        return north.isEmpty() ? Shapes.empty() : orient(north, state);
    }

    public static VoxelShape outlineShape(
            Plate plate,
            BlockState state,
            boolean hasDuvet,
            boolean hasCover,
            int largeStyle,
            int mediumMat,
            int smallMat,
            BedPlateSheetPillowSlots pillows,
            PickedLayer layer) {
        return switch (layer) {
            case SMALL, MEDIUM, LARGE -> pillowCell(plate, state, layer, pillows);
            case DUVET_COVER -> hasCover ? coverShape(plate, state) : bodyShape(plate, state);
            case DUVET -> hasDuvet ? duvetShape(plate, state) : bodyShape(plate, state);
            case BODY -> bodyShape(plate, state);
        };
    }

    private static boolean hitPillow(
            Plate plate,
            BlockState state,
            PickedLayer layer,
            BedPlateSheetPillowSlots pillows,
            Vec3 hitWorld,
            BlockPos pos) {
        VoxelShape cell = pillowCell(plate, state, layer, pillows);
        return !cell.isEmpty() && containsLocal(cell, hitWorld, pos);
    }

    /** 组合枕头盒已在床尾北向（含床头负 z），按格裁切，不再沿用床板6的整段平移。 */
    private static VoxelShape pillowCell(
            Plate plate, BlockState state, PickedLayer layer, BedPlateSheetPillowSlots pillows) {
        List<AABB> north = pillowBoxes(plate, layer, pillows);
        if (north.isEmpty()) {
            return Shapes.empty();
        }
        return orient(sliceCover(north, state.getValue(BedBlock.PART)), state);
    }

    private static List<AABB> pillowBoxes(Plate plate, PickedLayer layer, BedPlateSheetPillowSlots pillows) {
        if (pillows == null) {
            return List.of();
        }
        int plateId =
                switch (plate) {
                    case PLATE2 -> 2;
                    case PLATE3 -> 3;
                    case PLATE4 -> 4;
                };
        String prefix =
                switch (layer) {
                    case LARGE -> "large_";
                    case MEDIUM -> "medium_";
                    case SMALL -> "small_";
                    case BODY, DUVET, DUVET_COVER -> "";
                };
        if (prefix.isEmpty()) {
            return List.of();
        }
        List<AABB> out = new ArrayList<>();
        for (String suffix : BedPlateComboPillowLayout.suffixes(plateId, pillows)) {
            if (suffix.startsWith(prefix)) {
                out.addAll(BedPlateComboPillowPickShapes.of(plateId, suffix));
            }
        }
        return out;
    }

    private static VoxelShape wholeDuvet(VoxelShape foot, VoxelShape head) {
        return Shapes.or(foot, VoxelShapeTranslation.translate(head, 0.0, 0.0, -1.0));
    }

    private static VoxelShape duvetWhole(Plate plate) {
        return switch (plate) {
            case PLATE2 -> P2_DUVET_WHOLE;
            case PLATE3 -> P3_DUVET_WHOLE;
            case PLATE4 -> P4_DUVET_WHOLE;
        };
    }

    private static VoxelShape coverWhole(Plate plate) {
        return switch (plate) {
            case PLATE2 -> P2_COVER_WHOLE;
            case PLATE3 -> P3_COVER_WHOLE;
            case PLATE4 -> P4_COVER_WHOLE;
        };
    }

    private static VoxelShape unionBoxes(List<AABB> boxes) {
        VoxelShape out = Shapes.empty();
        for (AABB box : boxes) {
            if (box.getXsize() < 1.0E-4 || box.getYsize() < 1.0E-4 || box.getZsize() < 1.0E-4) {
                continue;
            }
            out = Shapes.or(out, Shapes.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
        }
        return out;
    }

    private static VoxelShape northDuvet(Plate plate, BedPart part) {
        boolean foot = part == BedPart.FOOT;
        return switch (plate) {
            case PLATE2 -> foot ? P2_DUVET_FOOT : P2_DUVET_HEAD;
            case PLATE3 -> foot ? P3_DUVET_FOOT : P3_DUVET_HEAD;
            case PLATE4 -> foot ? P4_DUVET_FOOT : P4_DUVET_HEAD;
        };
    }

    /** 被套 geo 在床尾北向；逐盒裁进本格，禁止先并集再 {@code toAabbs}。 */
    private static VoxelShape sliceCover(List<AABB> north, BedPart part) {
        boolean head = part == BedPart.HEAD;
        double z0 = head ? -1.0 : 0.0;
        double z1 = z0 + 1.0;
        VoxelShape out = Shapes.empty();
        for (AABB box : north) {
            double ix0 = Math.max(box.minX, 0.0);
            double iy0 = Math.max(box.minY, 0.0);
            double iz0 = Math.max(box.minZ, z0);
            double ix1 = Math.min(box.maxX, 1.0);
            double iy1 = Math.min(box.maxY, 1.0);
            double iz1 = Math.min(box.maxZ, z1);
            if (ix1 - ix0 < 1.0E-4 || iy1 - iy0 < 1.0E-4 || iz1 - iz0 < 1.0E-4) {
                continue;
            }
            out = Shapes.or(out, Shapes.box(ix0, iy0, iz0 - z0, ix1, iy1, iz1 - z0));
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
                north, state.getValue(BedBlock.FACING));
    }
}
