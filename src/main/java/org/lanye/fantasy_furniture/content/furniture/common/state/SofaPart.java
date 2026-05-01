package org.lanye.fantasy_furniture.content.furniture.common.state;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** 三连沙发在水平方向上的分段（相对 {@link net.minecraft.core.Direction} 朝向的左/中/右）。 */
public enum SofaPart implements StringRepresentable {
    LEFT,
    CENTER,
    RIGHT;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
