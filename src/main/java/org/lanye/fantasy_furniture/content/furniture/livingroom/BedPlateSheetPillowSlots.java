package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate3Block;
import org.lanye.reverie_core.geolib.bed.BedPlateSide;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;

/**
 * 床板1 / 3 / 4 型枕头槽。
 * 床单可选；大号至多两只；床板1型中号左右各一只，3/4型仍一只；小号一只（无被子也可落床面）。
 * 床板2/3/4 世界外形用各型号组合 geo，两只大号各占一组。
 */
public final class BedPlateSheetPillowSlots {

    private static final String NBT_LARGE1_STYLE = "Lg1Style";
    private static final String NBT_LARGE1_MAT = "Lg1Mat";
    private static final String NBT_LARGE2_STYLE = "Lg2Style";
    private static final String NBT_LARGE2_MAT = "Lg2Mat";
    private static final String NBT_MEDIUM = "MdPillow";
    private static final String NBT_MEDIUM_2 = "Md2Pillow";
    private static final String NBT_MEDIUM_SIDE = "MdSide";
    private static final String NBT_MEDIUM_2_POSE = "Md2Pose";
    private static final String NBT_SMALL = "SmPillow";
    private static final String NBT_LARGE1_FLAT = "Lg1Flat";
    private static final String NBT_LARGE2_FLAT = "Lg2Flat";
    private static final String NBT_MEDIUM_POSE = "MdPose";
    private static final String NBT_LARGE1_GEO = "Lg1Geo";
    private static final String NBT_LARGE2_GEO = "Lg2Geo";
    private static final String NBT_MEDIUM_GEO = "MdGeo";
    private static final String NBT_PILLOW_MODE = "PillowMode";
    /** 床板2/3/4 组合 geo：0=p1，1=s1，2=s2。-1 仍走自动。 */
    private static final String[] COMBO_GEOS = {"p1", "s1", "s2"};
    /** 床板2：整套共用一个字母。-1 表示不是这套规则。0=P，1=S，2=X。 */
    private int plate2Pose = -1;

    public void setPlate2Pose(int mode) {
        this.plate2Pose = mode >= 0 && mode <= 2 ? mode : -1;
    }

    public boolean hasPlate2Pose() {
        return plate2Pose >= 0;
    }

    private String plate2Letter() {
        return switch (plate2Pose) {
            case 2 -> "x";
            case 1 -> "s";
            default -> "p";
        };
    }

    /** 床板2小号：同一字母的第 1 槽。不是床板2整套摆放时返回 null。 */
    public String plate2SmallSuffix() {
        if (!hasPlate2Pose() || !hasSmall()) {
            return null;
        }
        return "small_" + plate2Letter() + "1";
    }

    private int large1StyleId;
    private int large1MaterialId;
    private int large2StyleId;
    private int large2MaterialId;
    /** 大号平放。缺省竖放，与已放置的床一致。 */
    private boolean large1Flat;
    private boolean large2Flat;
    /** 床板2/3/4：-1 自动，0=p1，1=s1，2=s2。 */
    private int large1Geo = -1;
    private int large2Geo = -1;
    private int mediumGeo = -1;
    private int medium1Mat;
    private int medium2Mat;
    private int medium3Mat;
    /** -1 跟随同侧大号；0 平放；1 竖放。床板3/4只用第一槽。 */
    private int medium1Pose = -1;
    private int medium2Pose = -1;
    private int smallMat;
    private int smallPlace = 4;

    public boolean hasLarge() {
        return hasLargeSlot(1) || hasLargeSlot(2);
    }

    public boolean hasLargeSlot(int index) {
        return index == 1 ? validLarge(large1StyleId, large1MaterialId) : validLarge(large2StyleId, large2MaterialId);
    }

    public int largeCount() {
        return (hasLargeSlot(1) ? 1 : 0) + (hasLargeSlot(2) ? 1 : 0);
    }

    /** 绘制用：优先第一只，没有则第二只。 */
    public int largeStyleId() {
        if (hasLargeSlot(1)) {
            return large1StyleId;
        }
        return hasLargeSlot(2) ? large2StyleId : 0;
    }

    public int largeMaterialId() {
        if (hasLargeSlot(1)) {
            return large1MaterialId;
        }
        return hasLargeSlot(2) ? large2MaterialId : 0;
    }

    public boolean hasMedium() {
        return hasMediumSlot(1) || hasMediumSlot(2) || hasMediumSlot(3);
    }

    public boolean hasMediumSlot(int side) {
        return BedPlate6MediumPillowMaterials.isValid(mediumMatRaw(side));
    }

    private int mediumMatRaw(int side) {
        return switch (side) {
            case 2 -> medium2Mat;
            case 3 -> medium3Mat;
            default -> medium1Mat;
        };
    }

    /** 第一槽优先。床板3/4只有这一只。 */
    public int mediumMat() {
        for (int side = 1; side <= 3; side++) {
            if (hasMediumSlot(side)) {
                return mediumMatRaw(side);
            }
        }
        return 0;
    }

    public int mediumMatOnSide(int side) {
        return hasMediumSlot(side) ? mediumMatRaw(side) : 0;
    }

    /** 只有一只时是它所在的列；多只时返回最小槽。 */
    public int mediumSide() {
        for (int side = 1; side <= 3; side++) {
            if (hasMediumSlot(side)) {
                return side;
            }
        }
        return 1;
    }

    public int mediumCount() {
        return (hasMediumSlot(1) ? 1 : 0) + (hasMediumSlot(2) ? 1 : 0) + (hasMediumSlot(3) ? 1 : 0);
    }

    /** 绘制用字母。床板1传 1，床板3/4传 34。 */
    public String poseLetter(int plate) {
        return BedPlatePillowMode.letter(resolvedMode(plate));
    }

    public int resolvedMode(int plate) {
        if (plate2Pose >= 0 && plate2Pose <= 2 && modeFits(plate, plate2Pose)) {
            return plate2Pose;
        }
        for (int mode = 0; mode < 3; mode++) {
            if (modeFits(plate, mode)) {
                return mode;
            }
        }
        return hasSmall() ? BedPlatePillowMode.TILTED : BedPlatePillowMode.FLAT;
    }

    /** @return 切换后的模式；没有可切换的枕头时 -1 */
    public int cycleSharedMode(int plate) {
        if (plate == 1 && !hasLarge() && !hasMedium()) {
            if (!hasSmall()) {
                return -1;
            }
            setPlate2Pose(BedPlatePillowMode.TILTED);
            return BedPlatePillowMode.TILTED;
        }
        if (!hasLarge() && !hasMedium() && !hasSmall()) {
            return -1;
        }
        int current = resolvedMode(plate);
        int next = current;
        for (int step = 1; step <= 3; step++) {
            int candidate = (current + step) % 3;
            if (modeFits(plate, candidate)) {
                next = candidate;
                break;
            }
        }
        setPlate2Pose(next);
        return next;
    }

    /** 当前字母放不下时，改成 P、S、X 里第一个放得下的。 */
    public void promoteMode(int plate) {
        if (plate2Pose >= 0 && modeFits(plate, plate2Pose)) {
            return;
        }
        if (plate != 1 && hasSmall() && !hasLarge() && !hasMedium() && modeFits(plate, BedPlatePillowMode.TILTED)) {
            setPlate2Pose(BedPlatePillowMode.TILTED);
            return;
        }
        for (int mode = 0; mode < 3; mode++) {
            if (modeFits(plate, mode)) {
                setPlate2Pose(mode);
                return;
            }
        }
    }

    public boolean canFitCounts(int plate, int large, int medium, int small) {
        for (int mode = 0; mode < 3; mode++) {
            int[] max = limits(plate, mode);
            if (large <= max[0] && medium <= max[1] && (plate == 1 || small <= max[2])) {
                return true;
            }
        }
        return false;
    }

    private boolean modeFits(int plate, int mode) {
        int[] max = limits(plate, mode);
        if (largeCount() > max[0] || mediumCount() > max[1]) {
            return false;
        }
        if (plate != 1 && (hasSmall() ? 1 : 0) > max[2]) {
            return false;
        }
        if (plate == 1) {
            return hasLarge() || hasMedium();
        }
        return hasLarge() || hasMedium() || hasSmall();
    }

    /** [大号, 中号, 小号]。床板1的小号不跟字母走。 */
    private static int[] limits(int plate, int mode) {
        if (plate == 1) {
            return switch (mode) {
                case BedPlatePillowMode.UPRIGHT -> new int[] {2, 3, 99};
                case BedPlatePillowMode.TILTED -> new int[] {0, 0, 99};
                default -> new int[] {2, 2, 99};
            };
        }
        return switch (mode) {
            case BedPlatePillowMode.UPRIGHT -> new int[] {2, 2, 1};
            case BedPlatePillowMode.TILTED -> new int[] {0, 0, 1};
            default -> new int[] {1, 1, 0};
        };
    }
    public String plate2MediumSuffix(int slot) {
        if (!hasPlate2Pose() || !hasMediumSlot(slot)) {
            return null;
        }
        return plate2Letter() + slot;
    }

    public int smallPlace() {
        return smallPlace >= 4 && smallPlace <= 7 ? smallPlace : 4;
    }

    public boolean hasSmall() {
        return BedPlate6SmallPillowMaterials.isValid(smallMat);
    }

    public int smallMat() {
        return hasSmall() ? smallMat : 0;
    }

    public boolean hasAny() {
        return hasLarge() || hasMedium() || hasSmall();
    }

    public boolean canAddLarge() {
        return largeCount() < 2;
    }

    public boolean tryAddLarge(int styleId, int materialId) {
        if (!canAddLarge() || !validLarge(styleId, materialId)) {
            return false;
        }
        if (!hasLargeSlot(1)) {
            return writeLargeSlot(1, styleId, materialId);
        }
        return writeLargeSlot(2, styleId, materialId);
    }

    /**
     * 床板1型：1=左列（组合组 S1），2=右列（组合组 S2）。该列已有大号则失败。
     * S1 在导出里靠 +X，默认 Geo 会做 X 镜像，画到床尾右锚点的 −X，也就是面朝与床正面相同方向时的左手边。
     */
    public boolean canAddLargeSide(int side) {
        return side == 2 ? !hasLargeSlot(2) : !hasLargeSlot(1);
    }

    public boolean tryAddLargeSide(int side, int styleId, int materialId) {
        if (!canAddLargeSide(side) || !validLarge(styleId, materialId)) {
            return false;
        }
        return writeLargeSlot(side == 2 ? 2 : 1, styleId, materialId);
    }

    public int largeStyleOnSide(int side) {
        return side == 2 ? (hasLargeSlot(2) ? large2StyleId : 0) : (hasLargeSlot(1) ? large1StyleId : 0);
    }

    public int largeMaterialOnSide(int side) {
        return side == 2
                ? (hasLargeSlot(2) ? large2MaterialId : 0)
                : (hasLargeSlot(1) ? large1MaterialId : 0);
    }

    /** 床板1型大号：true 平放（DAP），false 竖放（S）。 */
    public boolean largeFlat(int side) {
        if (!hasLargeSlot(side)) {
            return false;
        }
        return side == 2 ? large2Flat : large1Flat;
    }

    /** @return 切换后是否平放 */
    public boolean toggleLargeFlat(int side) {
        if (side == 2) {
            large2Flat = hasLargeSlot(2) && !large2Flat;
            return large2Flat;
        }
        large1Flat = hasLargeSlot(1) && !large1Flat;
        return large1Flat;
    }

    /** 中号：调试指定优先，否则同侧大号竖放时竖放、否则平放。 */
    public boolean mediumStanding() {
        return mediumStanding(mediumSide());
    }

    public boolean mediumStanding(int side) {
        if (!hasMediumSlot(side)) {
            return false;
        }
        int pose = side == 2 ? medium2Pose : medium1Pose;
        if (pose == 0) {
            return false;
        }
        if (pose == 1) {
            return true;
        }
        return hasLargeSlot(side) && !largeFlat(side);
    }

    /** @return 切换后是否竖放 */
    public boolean toggleMediumStanding() {
        return toggleMediumStanding(mediumSide());
    }

    public boolean toggleMediumStanding(int side) {
        if (!hasMediumSlot(side)) {
            return false;
        }
        boolean standing = !mediumStanding(side);
        if (side == 2) {
            medium2Pose = standing ? 1 : 0;
        } else {
            medium1Pose = standing ? 1 : 0;
        }
        return standing;
    }

    /** 床板3/4：第一槽，放不下再占第二槽。床板1走 {@link #tryAddMediumSide}。 */
    public boolean canAddMedium() {
        if (hasMediumSlot(1) && hasMediumSlot(2)) {
            return false;
        }
        return canFitCounts(34, largeCount(), mediumCount() + 1, hasSmall() ? 1 : 0);
    }

    /** 床板1型：该列还没有中号就能放。 */
    public boolean canAddMediumSide(int side) {
        int slot = side == 3 ? 3 : side == 2 ? 2 : 1;
        return !hasMediumSlot(slot);
    }

    public boolean tryAddMedium(int materialId) {
        if (!BedPlate6MediumPillowMaterials.isValid(materialId)) {
            return false;
        }
        if (!hasMediumSlot(1)) {
            return tryAddMediumSide(1, materialId);
        }
        if (!hasMediumSlot(2)) {
            return tryAddMediumSide(2, materialId);
        }
        return false;
    }

    public boolean tryAddMediumSide(int side, int materialId) {
        if (!canAddMediumSide(side) || !BedPlate6MediumPillowMaterials.isValid(materialId)) {
            return false;
        }
        if (side == 3) {
            medium3Mat = materialId;
        } else if (side == 2) {
            medium2Mat = materialId;
            medium2Pose = -1;
        } else {
            medium1Mat = materialId;
            medium1Pose = -1;
        }
        return true;
    }

    public boolean canAddSmall() {
        return !hasSmall();
    }

    public boolean tryAddSmall(int materialId) {
        return tryAddSmallPlace(4, materialId);
    }

    public boolean tryAddSmallPlace(int place, int materialId) {
        if (!canAddSmall() || !BedPlate6SmallPillowMaterials.isValid(materialId)) {
            return false;
        }
        this.smallMat = materialId;
        this.smallPlace = place >= 4 && place <= 7 ? place : 4;
        return true;
    }

    public int largeGeo(int side) {
        return side == 2 ? large2Geo : large1Geo;
    }

    public void setLargeGeo(int side, int geo) {
        int stored = geo >= 0 && geo < COMBO_GEOS.length ? geo : -1;
        if (side == 2) {
            large2Geo = stored;
        } else {
            large1Geo = stored;
        }
    }

    public int mediumGeo() {
        return mediumGeo;
    }

    public void setMediumGeo(int geo) {
        mediumGeo = geo >= 0 && geo < COMBO_GEOS.length ? geo : -1;
    }

    /** 不含 {@code large_} 前缀。该槽没有大号时返回 null。 */
    public String largeSuffix(int side) {
        if (!hasLargeSlot(side)) {
            return null;
        }
        if (hasPlate2Pose()) {
            int ordinal = side == 2 && hasLargeSlot(1) ? 2 : 1;
            return plate2Letter() + ordinal;
        }
        int geo = largeGeo(side);
        if (geo >= 0 && geo < COMBO_GEOS.length) {
            return COMBO_GEOS[geo];
        }
        if (largeCount() >= 2) {
            return side == 2 ? "s2" : "s1";
        }
        boolean propped = hasMediumSlot(side);
        return propped ? "s" + side : "p1";
    }

    /** 不含 {@code medium_} 前缀。没有中号时返回 null。 */
    public String mediumSuffix() {
        if (!hasMedium()) {
            return null;
        }
        if (hasPlate2Pose()) {
            return plate2Letter() + "1";
        }
        if (mediumGeo >= 0 && mediumGeo < COMBO_GEOS.length) {
            return COMBO_GEOS[mediumGeo];
        }
        int side = mediumSide();
        return hasLargeSlot(side) ? "s" + side : "p1";
    }

    /** @return 切换后的后缀（p1 / s1 / s2），该槽没有大号则 null */
    public String cycleLarge(int side) {
        String current = largeSuffix(side);
        if (current == null) {
            return null;
        }
        int next = nextComboGeo(current);
        setLargeGeo(side, next);
        return COMBO_GEOS[next];
    }

    /** @return 切换后的后缀（p1 / s1 / s2），没有中号则 null */
    public String cycleMedium() {
        String current = mediumSuffix();
        if (current == null) {
            return null;
        }
        int next = nextComboGeo(current);
        setMediumGeo(next);
        return COMBO_GEOS[next];
    }

    private static int nextComboGeo(String current) {
        int idx = 0;
        for (int i = 0; i < COMBO_GEOS.length; i++) {
            if (COMBO_GEOS[i].equals(current)) {
                idx = i;
                break;
            }
        }
        return (idx + 1) % COMBO_GEOS.length;
    }

    public void clearLargeSlot(int side) {
        if (side == 2) {
            large2StyleId = 0;
            large2MaterialId = 0;
            large2Flat = false;
            large2Geo = -1;
            return;
        }
        large1StyleId = 0;
        large1MaterialId = 0;
        large1Flat = false;
        large1Geo = -1;
    }

    public void clearMedium() {
        clearMediumSlot(1);
        clearMediumSlot(2);
        clearMediumSlot(3);
    }

    public void clearMediumSlot(int side) {
        if (side == 3) {
            medium3Mat = 0;
            return;
        }
        if (side == 2) {
            medium2Mat = 0;
            medium2Pose = -1;
            return;
        }
        medium1Mat = 0;
        medium1Pose = -1;
        mediumGeo = -1;
    }

    public void clearSmall() {
        smallMat = 0;
        smallPlace = 4;
    }

    public void clear() {
        large1StyleId = 0;
        large1MaterialId = 0;
        large1Flat = false;
        large1Geo = -1;
        large2StyleId = 0;
        large2MaterialId = 0;
        large2Flat = false;
        large2Geo = -1;
        medium1Mat = 0;
        medium2Mat = 0;
        medium3Mat = 0;
        medium1Pose = -1;
        medium2Pose = -1;
        mediumGeo = -1;
        smallMat = 0;
        smallPlace = 4;
        plate2Pose = -1;
    }

    public void write(CompoundTag tag) {
        writeLarge(tag, NBT_LARGE1_STYLE, NBT_LARGE1_MAT, large1StyleId, large1MaterialId);
        writeLarge(tag, NBT_LARGE2_STYLE, NBT_LARGE2_MAT, large2StyleId, large2MaterialId);
        if (large1Flat && validLarge(large1StyleId, large1MaterialId)) {
            tag.putBoolean(NBT_LARGE1_FLAT, true);
        }
        if (large2Flat && validLarge(large2StyleId, large2MaterialId)) {
            tag.putBoolean(NBT_LARGE2_FLAT, true);
        }
        if (large1Geo >= 0 && validLarge(large1StyleId, large1MaterialId)) {
            tag.putInt(NBT_LARGE1_GEO, large1Geo);
        }
        if (large2Geo >= 0 && validLarge(large2StyleId, large2MaterialId)) {
            tag.putInt(NBT_LARGE2_GEO, large2Geo);
        }
        if (hasMediumSlot(1)) {
            tag.putInt(NBT_MEDIUM, medium1Mat);
            tag.putInt(NBT_MEDIUM_SIDE, 1);
            if (medium1Pose == 0 || medium1Pose == 1) {
                tag.putInt(NBT_MEDIUM_POSE, medium1Pose);
            }
            if (mediumGeo >= 0) {
                tag.putInt(NBT_MEDIUM_GEO, mediumGeo);
            }
        }
        if (hasMediumSlot(2)) {
            tag.putInt(NBT_MEDIUM_2, medium2Mat);
            if (medium2Pose == 0 || medium2Pose == 1) {
                tag.putInt(NBT_MEDIUM_2_POSE, medium2Pose);
            }
        }
        if (smallMat != 0) {
            tag.putInt(NBT_SMALL, smallMat);
            tag.putInt("SmPlace", smallPlace);
        }
        if (plate2Pose >= 0) {
            tag.putInt(NBT_PILLOW_MODE, plate2Pose);
        }
    }

    public void read(CompoundTag tag) {
        large1StyleId = tag.getInt(NBT_LARGE1_STYLE);
        large1MaterialId = tag.getInt(NBT_LARGE1_MAT);
        if (!validLarge(large1StyleId, large1MaterialId)) {
            large1StyleId = 0;
            large1MaterialId = 0;
            large1Flat = false;
            large1Geo = -1;
        } else {
            large1Flat = tag.getBoolean(NBT_LARGE1_FLAT);
            large1Geo = readComboGeo(tag, NBT_LARGE1_GEO);
        }
        large2StyleId = tag.getInt(NBT_LARGE2_STYLE);
        large2MaterialId = tag.getInt(NBT_LARGE2_MAT);
        if (!validLarge(large2StyleId, large2MaterialId)) {
            large2StyleId = 0;
            large2MaterialId = 0;
            large2Flat = false;
            large2Geo = -1;
        } else {
            large2Flat = tag.getBoolean(NBT_LARGE2_FLAT);
            large2Geo = readComboGeo(tag, NBT_LARGE2_GEO);
        }
        medium1Mat = 0;
        medium2Mat = 0;
        medium3Mat = 0;
        medium1Pose = -1;
        medium2Pose = -1;
        mediumGeo = -1;
        int stored = tag.getInt(NBT_MEDIUM);
        int pose = tag.contains(NBT_MEDIUM_POSE) ? tag.getInt(NBT_MEDIUM_POSE) : -1;
        pose = pose == 0 || pose == 1 ? pose : -1;
        if (BedPlate6MediumPillowMaterials.isValid(stored)) {
            if (!tag.contains(NBT_MEDIUM_2) && tag.getInt(NBT_MEDIUM_SIDE) == 2) {
                medium2Mat = stored;
                medium2Pose = pose;
            } else {
                medium1Mat = stored;
                medium1Pose = pose;
                mediumGeo = readComboGeo(tag, NBT_MEDIUM_GEO);
            }
        }
        int stored2 = tag.getInt(NBT_MEDIUM_2);
        if (BedPlate6MediumPillowMaterials.isValid(stored2)) {
            medium2Mat = stored2;
            int pose2 = tag.contains(NBT_MEDIUM_2_POSE) ? tag.getInt(NBT_MEDIUM_2_POSE) : -1;
            medium2Pose = pose2 == 0 || pose2 == 1 ? pose2 : -1;
        }
        smallMat = tag.getInt(NBT_SMALL);
        if (!BedPlate6SmallPillowMaterials.isValid(smallMat)) {
            smallMat = 0;
            smallPlace = 4;
        } else {
            int place = tag.getInt("SmPlace");
            smallPlace = place >= 4 && place <= 7 ? place : 4;
        }
        plate2Pose = -1;
        if (tag.contains(NBT_PILLOW_MODE)) {
            setPlate2Pose(tag.getInt(NBT_PILLOW_MODE));
        } else if (hasLarge() || hasMedium()) {
            boolean upright = largeCount() >= 2
                    || mediumCount() >= 2
                    || large1Geo >= 1
                    || large2Geo >= 1
                    || mediumGeo >= 1
                    || (hasLargeSlot(1) && !large1Flat)
                    || (hasLargeSlot(2) && !large2Flat)
                    || medium1Pose == 1
                    || medium2Pose == 1;
            setPlate2Pose(upright ? BedPlatePillowMode.UPRIGHT : BedPlatePillowMode.FLAT);
        } else if (hasSmall()) {
            setPlate2Pose(
                    smallPlace == 6 || smallPlace == 7 ? BedPlatePillowMode.UPRIGHT : BedPlatePillowMode.TILTED);
        }
    }

    public void collect(List<ItemStack> stacks) {
        if (hasLargeSlot(1)) {
            add(stacks, BedPlate6LargePillowItem.stackForRegistry(large1StyleId, large1MaterialId));
        }
        if (hasLargeSlot(2)) {
            add(stacks, BedPlate6LargePillowItem.stackForRegistry(large2StyleId, large2MaterialId));
        }
        if (hasMediumSlot(1)) {
            add(stacks, BedPlate6MediumPillowItem.stackForRegistry(medium1Mat));
        }
        if (hasMediumSlot(2)) {
            add(stacks, BedPlate6MediumPillowItem.stackForRegistry(medium2Mat));
        }
        if (hasMediumSlot(3)) {
            add(stacks, BedPlate6MediumPillowItem.stackForRegistry(medium3Mat));
        }
        if (hasSmall()) {
            add(stacks, BedPlate6SmallPillowItem.stackForRegistry(smallMat));
        }
    }

    public List<ItemStack> collect() {
        List<ItemStack> stacks = new ArrayList<>();
        collect(stacks);
        return stacks;
    }

    public static InteractionResult applyLarge(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return apply(level, pos, state, player, hand, Kind.LARGE);
    }

    public static InteractionResult applyMedium(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return apply(level, pos, state, player, hand, Kind.MEDIUM);
    }

    public static InteractionResult applySmall(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        return apply(level, pos, state, player, hand, Kind.SMALL);
    }

    private static InteractionResult apply(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, Kind kind) {
        BedPlateSheetPillowHost host = host(level, state, pos);
        if (host == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        int side = plate1Side(state);
        if (!level.isClientSide) {
            boolean added =
                    switch (kind) {
                        case LARGE -> {
                            if (!(stack.getItem() instanceof BedPlate6LargePillowItem held)) {
                                yield false;
                            }
                            BedPlateSheetPillowSlots slots = host.sheetPillows();
                            yield side == 0
                                    ? slots.tryAddLarge(held.getStyleId(), held.getMaterialId())
                                    : slots.tryAddLargeSide(side, held.getStyleId(), held.getMaterialId());
                        }
                        case MEDIUM -> {
                            if (!(stack.getItem() instanceof BedPlate6MediumPillowItem held)) {
                                yield false;
                            }
                            BedPlateSheetPillowSlots slots = host.sheetPillows();
                            yield side == 0
                                    ? slots.tryAddMedium(held.getMaterialId())
                                    : slots.tryAddMediumSide(side, held.getMaterialId())
                                            || slots.tryAddMediumSide(3, held.getMaterialId());
                        }
                        case SMALL -> {
                            if (!(stack.getItem() instanceof BedPlate6SmallPillowItem held)) {
                                yield false;
                            }
                            yield side == 0
                                    ? host.sheetPillows().tryAddSmall(held.getMaterialId())
                                    : host.sheetPillows()
                                            .tryAddSmallPlace(
                                                    smallPlaceFor(host.sheetPillows(), side),
                                                    held.getMaterialId());
                        }
                    };
            if (!added) {
                return InteractionResult.FAIL;
            }
            host.sheetPillows().promoteMode(state.getBlock() instanceof BedPlate1Block ? 1 : 34);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            host.syncSheetPillows();
        } else if (!previewCanAdd(host, stack, kind, side)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean previewCanAdd(
            BedPlateSheetPillowHost host, ItemStack stack, Kind kind, int side) {
        BedPlateSheetPillowSlots slots = host.sheetPillows();
        return switch (kind) {
            case LARGE -> stack.getItem() instanceof BedPlate6LargePillowItem
                    && (side == 0 ? slots.canAddLarge() : slots.canAddLargeSide(side));
            case MEDIUM -> {
                if (!(stack.getItem() instanceof BedPlate6MediumPillowItem)) {
                    yield false;
                }
                int plate = side == 0 ? 34 : 1;
                int small = slots.hasSmall() ? 1 : 0;
                if (!slots.canFitCounts(plate, slots.largeCount(), slots.mediumCount() + 1, small)) {
                    yield false;
                }
                if (side == 0) {
                    yield slots.canAddMedium();
                }
                yield slots.canAddMediumSide(side) || slots.canAddMediumSide(3);
            }
            case SMALL -> stack.getItem() instanceof BedPlate6SmallPillowItem && slots.canAddSmall();
        };
    }

    /**
     * 0=不是床板1型；1=左列；2=右列。
     * 左右跟床的朝向走：面朝与床正面相同的方向时，左手边是左列。不要按“正对着床头看”来镜像。
     */
    private static int plate1Side(BlockState state) {
        if (!(state.getBlock() instanceof BedPlate1Block)) {
            return 0;
        }
        return state.getValue(BedPlate1Block.SIDE) == BedPlateSide.LEFT ? 1 : 2;
    }

    private static int smallPlaceFor(BedPlateSheetPillowSlots slots, int side) {
        if (side == 2) {
            return slots.hasLargeSlot(2) ? 7 : 5;
        }
        return slots.hasLargeSlot(1) ? 6 : 4;
    }

    private static BedPlateSheetPillowHost host(Level level, BlockState state, BlockPos pos) {
        BlockEntity be;
        if (state.getBlock() instanceof BedPlate1Block) {
            be = BedPlate1Block.decorEntity(level, state, pos);
        } else if (state.getBlock() instanceof BedPlate3Block
                || state.is(ModBlocks.BED_PLATE4.block().get())) {
            be = level.getBlockEntity(BedPlateBedFootPos.footPos(state, pos));
        } else {
            return null;
        }
        return be instanceof BedPlateSheetPillowHost found ? found : null;
    }

    private static int readComboGeo(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return -1;
        }
        int geo = tag.getInt(key);
        return geo >= 0 && geo < COMBO_GEOS.length ? geo : -1;
    }

    private static boolean validLarge(int styleId, int materialId) {
        return BedPlate6LargePillowStyles.isValid(styleId)
                && BedPlate6DuvetMaterials.isValid(materialId)
                && !BedPlate6LargePillowItem.isUnavailableLargeVariant(styleId, materialId);
    }

    private boolean writeLargeSlot(int side, int styleId, int materialId) {
        if (side == 2) {
            large2StyleId = styleId;
            large2MaterialId = materialId;
            large2Flat = false;
        } else {
            large1StyleId = styleId;
            large1MaterialId = materialId;
            large1Flat = false;
        }
        return true;
    }

    private static void writeLarge(CompoundTag tag, String styleKey, String matKey, int styleId, int materialId) {
        if (styleId != 0) {
            tag.putInt(styleKey, styleId);
            tag.putInt(matKey, materialId);
        }
    }

    private static void add(List<ItemStack> stacks, ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }

    private enum Kind {
        LARGE,
        MEDIUM,
        SMALL
    }
}
