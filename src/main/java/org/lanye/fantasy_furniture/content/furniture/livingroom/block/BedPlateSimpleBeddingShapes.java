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
        LARGE_1,
        LARGE_2,
        LARGE_3,
        MEDIUM_1,
        MEDIUM_2,
        MEDIUM_3,
        SMALL,
        SMALL_3,
        SMALL_4;

        /** 同类型的两只枕头是两个槽，不是一个并集。0 表示这一层不分槽。 */
        public int slot() {
            return switch (this) {
                case LARGE_1, MEDIUM_1 -> 1;
                case LARGE_2, MEDIUM_2 -> 2;
                case LARGE_3, MEDIUM_3, SMALL_3 -> 3;
                case SMALL_4 -> 4;
                default -> 0;
            };
        }

        public boolean isPillow() {
            return this == SMALL || this == SMALL_3 || this == SMALL_4
                    || this == LARGE_1 || this == LARGE_2 || this == LARGE_3
                    || this == MEDIUM_1 || this == MEDIUM_2 || this == MEDIUM_3;
        }
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
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.LARGE_1, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.LARGE_2, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.LARGE_3, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.MEDIUM_1, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.MEDIUM_2, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.MEDIUM_3, pillows));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.SMALL, pillows, hasCover));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.SMALL_3, pillows, hasCover));
        s = Shapes.or(s, pillowCell(plate, state, PickedLayer.SMALL_4, pillows, hasCover));
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
        if (hitPillow(plate, state, PickedLayer.SMALL_3, pillows, hasCover, hitWorld, pos)) {
            return PickedLayer.SMALL_3;
        }
        if (hitPillow(plate, state, PickedLayer.SMALL_4, pillows, hasCover, hitWorld, pos)) {
            return PickedLayer.SMALL_4;
        }
        if (hitPillow(plate, state, PickedLayer.SMALL, pillows, hasCover, hitWorld, pos)) {
            return PickedLayer.SMALL;
        }
        if (hitPillow(plate, state, PickedLayer.MEDIUM_1, pillows, hitWorld, pos)) {
            return PickedLayer.MEDIUM_1;
        }
        if (hitPillow(plate, state, PickedLayer.MEDIUM_2, pillows, hitWorld, pos)) {
            return PickedLayer.MEDIUM_2;
        }
        if (hitPillow(plate, state, PickedLayer.MEDIUM_3, pillows, hitWorld, pos)) {
            return PickedLayer.MEDIUM_3;
        }
        if (hitPillow(plate, state, PickedLayer.LARGE_1, pillows, hitWorld, pos)) {
            return PickedLayer.LARGE_1;
        }
        if (hitPillow(plate, state, PickedLayer.LARGE_2, pillows, hitWorld, pos)) {
            return PickedLayer.LARGE_2;
        }
        if (hitPillow(plate, state, PickedLayer.LARGE_3, pillows, hitWorld, pos)) {
            return PickedLayer.LARGE_3;
        }
        if (hasCover && containsLocal(coverShape(plate, state), hitWorld, pos)) {
            return PickedLayer.DUVET_COVER;
        }
        if (hasDuvet && containsLocal(duvetShape(plate, state), hitWorld, pos)) {
            return PickedLayer.DUVET;
        }
        return PickedLayer.BODY;
    }

    /**
     * 组件描边：相对床尾的整件（不裁格、不含木架）。床体返回空，由调用方画命中格。
     * 枕头只描准心命中的那一槽。
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
                    case SMALL, SMALL_3, SMALL_4, MEDIUM_1, MEDIUM_2, MEDIUM_3, LARGE_1, LARGE_2, LARGE_3 ->
                            unionBoxes(pillowBoxes(plate, layer, pillows, hasCover));
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
            case SMALL, SMALL_3, SMALL_4, MEDIUM_1, MEDIUM_2, MEDIUM_3, LARGE_1, LARGE_2, LARGE_3 ->
                    pillowCell(plate, state, layer, pillows, hasCover);
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
        return hitPillow(plate, state, layer, pillows, false, hitWorld, pos);
    }

    private static boolean hitPillow(
            Plate plate,
            BlockState state,
            PickedLayer layer,
            BedPlateSheetPillowSlots pillows,
            boolean hasCover,
            Vec3 hitWorld,
            BlockPos pos) {
        VoxelShape cell = pillowCell(plate, state, layer, pillows, hasCover);
        return !cell.isEmpty() && containsLocal(cell, hitWorld, pos);
    }

    /** 组合枕头盒已在床尾北向（含床头负 z），按格裁切，不再沿用床板6的整段平移。 */
    private static VoxelShape pillowCell(
            Plate plate, BlockState state, PickedLayer layer, BedPlateSheetPillowSlots pillows) {
        return pillowCell(plate, state, layer, pillows, false);
    }

    private static VoxelShape pillowCell(
            Plate plate,
            BlockState state,
            PickedLayer layer,
            BedPlateSheetPillowSlots pillows,
            boolean hasCover) {
        List<AABB> north = pillowBoxes(plate, layer, pillows, hasCover);
        if (north.isEmpty()) {
            return Shapes.empty();
        }
        return orient(sliceCover(north, state.getValue(BedBlock.PART)), state);
    }

    private static List<AABB> pillowBoxes(
            Plate plate, PickedLayer layer, BedPlateSheetPillowSlots pillows, boolean hasCover) {
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
                    case LARGE_1, LARGE_2, LARGE_3 -> "large_";
                    case MEDIUM_1, MEDIUM_2, MEDIUM_3 -> "medium_";
                    case SMALL, SMALL_3, SMALL_4 -> "small_";
                    case BODY, DUVET, DUVET_COVER -> "";
                };
        if (prefix.isEmpty()) {
            return List.of();
        }
        int slot = layer.slot();
        List<AABB> out = new ArrayList<>();
        for (String suffix : BedPlateComboPillowLayout.suffixes(plateId, pillows, hasCover)) {
            if (!suffix.startsWith(prefix)) {
                continue;
            }
            if (slot != 0 && suffixSlot(suffix) != slot) {
                continue;
            }
            out.addAll(BedPlateComboPillowPickShapes.of(plateId, suffix));
        }
        return out;
    }

    /** 组合后缀末位是槽位号。{@code _cover} 不算槽。没有数字时当作槽 1。 */
    private static int suffixSlot(String suffix) {
        String bare = suffix.endsWith("_cover") ? suffix.substring(0, suffix.length() - "_cover".length()) : suffix;
        char last = bare.charAt(bare.length() - 1);
        return last >= '1' && last <= '4' ? last - '0' : 1;
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
