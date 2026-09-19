package org.lanye.fantasy_furniture.content.furniture.livingroom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** 全体床品共用：P 平放、S 竖放、X 斜放。数字是槽位，不是另一种摆法。 */
public final class BedPlatePillowMode {

    public static final int FLAT = 0;
    public static final int UPRIGHT = 1;
    public static final int TILTED = 2;

    private BedPlatePillowMode() {}

    public static String letter(int mode) {
        return switch (mode) {
            case TILTED -> "x";
            case UPRIGHT -> "s";
            default -> "p";
        };
    }

    public static void tell(Player player, int mode) {
        String key =
                switch (mode) {
                    case TILTED -> "debug.fantasy_furniture.pillow_mode.x";
                    case UPRIGHT -> "debug.fantasy_furniture.pillow_mode.s";
                    default -> "debug.fantasy_furniture.pillow_mode.p";
                };
        player.displayClientMessage(
                Component.translatable("debug.fantasy_furniture.variant.bed_plate_pillow_mode", Component.translatable(key)),
                true);
    }
}
