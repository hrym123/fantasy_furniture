package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;

/**
 * 床板2/3/4 枕头用哪一组组合 geo。
 * 后缀与导出文件 {@code bed_plateN_pillow_<后缀>.geo.json} 一致，不在渲染里平移。
 * 床板2/3/4：整套枕头共用一个字母（P/S/X），数字就是槽位，不把后一槽画成前一槽。
 * 床板1不走这里：它按左右列，不按填满顺序。
 */
public final class BedPlateComboPillowLayout {

    private BedPlateComboPillowLayout() {}

    public static List<String> suffixes(int plate, BedPlateSheetPillowSlots slots) {
        return suffixes(plate, slots, false);
    }

    public static List<String> suffixes(int plate, BedPlateSheetPillowSlots slots, boolean hasCover) {
        List<String> out = new ArrayList<>();
        if (slots == null) {
            return out;
        }
        if (plate == 3 || plate == 4) {
            slots.adoptSmallSlots(plate);
        }
        if (plate != 2) {
            slots.setPlate2Pose(slots.resolvedMode(plate == 1 ? 1 : 34));
        }
        boolean large1 = slots.hasLargeSlot(1);
        boolean large2 = slots.hasLargeSlot(2);
        if (large1) {
            out.add("large_" + slots.largeSuffix(1));
        }
        if (large2) {
            out.add("large_" + slots.largeSuffix(2));
        }
        if (slots.hasLargeSlot(3)) {
            out.add("large_" + slots.largeSuffix(3));
        }
        if (slots.hasPlate2Pose()) {
            for (int slot = 1; slot <= 3; slot++) {
                String medium = slots.plate2MediumSuffix(slot);
                if (medium != null) {
                    out.add("medium_" + medium);
                }
            }
        } else if (slots.hasMedium()) {
            out.add("medium_" + slots.mediumSuffix());
        }
        if (plate == 3 || plate == 4) {
            for (int slot = 3; slot <= 4; slot++) {
                if (slots.hasSmallSlot(slot)) {
                    out.add("small_x" + slot + (hasCover ? "_cover" : ""));
                }
            }
        } else if (slots.hasSmall()) {
            String plate2Small = slots.plate2SmallSuffix();
            out.add(plate2Small != null ? plate2Small : smallSuffix(plate, slots));
        }
        return out;
    }

    /** 床板2 小号跟随整床字母，槽位是 1。床板3/4 不走这里。 */
    private static String smallSuffix(int plate, BedPlateSheetPillowSlots slots) {
        int place = slots.smallPlace();
        if (plate == 2) {
            boolean foot = place == 5 || place == 7;
            boolean raised = place >= 6 || (foot ? slots.hasLargeSlot(2) : slots.hasLarge());
            if (foot) {
                return raised ? "small_s2" : "small_p1";
            }
            return raised ? "small_s1" : "small_p2";
        }
        return switch (place) {
            case 5 -> "small_x2";
            case 6 -> "small_s1";
            case 7 -> "small_s2";
            default -> slots.hasLarge() ? "small_s1" : "small_x1";
        };
    }
}
