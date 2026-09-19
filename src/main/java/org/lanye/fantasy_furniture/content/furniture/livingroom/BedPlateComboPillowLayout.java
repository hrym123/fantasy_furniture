package org.lanye.fantasy_furniture.content.furniture.livingroom;

import java.util.ArrayList;
import java.util.List;

/**
 * 床板2/3/4 枕头用哪一组组合 geo。
 * 后缀与导出文件 {@code bed_plateN_pillow_<后缀>.geo.json} 一致，不在渲染里平移。
 * 一只大号且中号不靠在该槽上用平放 {@code large_p1}；两只大号或中号竖放时用 {@code large_s1}/{@code large_s2}。
 */
public final class BedPlateComboPillowLayout {

    private BedPlateComboPillowLayout() {}

    public static List<String> suffixes(int plate, BedPlateSheetPillowSlots slots) {
        List<String> out = new ArrayList<>();
        if (slots == null) {
            return out;
        }
        boolean large1 = slots.hasLargeSlot(1);
        boolean large2 = slots.hasLargeSlot(2);
        if (large1 && large2) {
            out.add("large_s1");
            out.add("large_s2");
        } else if (large1 || large2) {
            int side = large1 ? 1 : 2;
            boolean propped = slots.hasMedium() && slots.mediumSide() == side;
            out.add(propped ? "large_s" + side : "large_p1");
        }
        if (slots.hasMedium()) {
            int side = slots.mediumSide();
            boolean standing = slots.hasLargeSlot(side);
            out.add(standing ? "medium_s" + side : "medium_p1");
        }
        if (slots.hasSmall()) {
            out.add(smallSuffix(plate, slots));
        }
        return out;
    }

    /** 床板2 小号是 P/S；床板3/4 是 X（落床面）/ S（落在大号上）。 */
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
