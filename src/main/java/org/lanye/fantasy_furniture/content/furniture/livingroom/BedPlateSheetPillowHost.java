package org.lanye.fantasy_furniture.content.furniture.livingroom;

/** 床板1 / 3 / 4 型方块实体上的枕头槽。床单不是放置前置。 */
public interface BedPlateSheetPillowHost {

    BedPlateSheetPillowSlots sheetPillows();

    boolean hasDuvet();

    void syncSheetPillows();
}
