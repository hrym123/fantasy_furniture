package org.lanye.fantasy_furniture.content.furniture.common.state;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/** 卡座：直段或拐角（前方邻格拼 L；左拼 / 右拼对应不同 Y 旋转）。 */
public enum BanquetteShape implements StringRepresentable {
    STRAIGHT,
    /** 前方卡座朝向相对本格为 {@link net.minecraft.core.Direction#getClockWise()}（左拼 L）。 */
    CORNER_LEFT,
    /** 前方卡座朝向相对本格为 {@link net.minecraft.core.Direction#getCounterClockWise()}（右拼 L）。 */
    CORNER_RIGHT;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
