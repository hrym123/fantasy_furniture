package org.lanye.fantasy_furniture.content.furniture.common.client;

import org.lanye.fantasy_furniture.bootstrap.block.PlainWindowBlocks.Material;

/**
 * 普通窗户共用一套母版贴图，在模型面上使用 {@code tintindex: 0}，由客户端在此返回染色（创造栏 9 色区分）。
 */
public final class PlainWindowColors {

    private static volatile boolean loggedTint;

    private PlainWindowColors() {}

    /** 与纹理相乘的 RGB（含不透明 alpha），偏淡色以便仍像玻璃窗。 */
    public static int tintRgb(Material material) {
        // #region agent log
        if (!loggedTint) {
            loggedTint = true;
            AgentDebugLog.log(
                    "H2",
                    "PlainWindowColors.tintRgb",
                    "first tint sample",
                    "{\"material\":\"" + material.name() + "\"}");
        }
        // #endregion
        return switch (material) {
            case WHITE -> 0xFFF7F7F7;
            case CREAM -> 0xFFF3E5C9;
            case ROSE -> 0xFFFFC2CB;
            case MINT -> 0xFFBFE8D8;
            case SKY -> 0xFFA9D4F2;
            case LAVENDER -> 0xFFD6C9F0;
            case PEACH -> 0xFFFFDCC4;
            case COCOA -> 0xFFE8C8A8;
            case SILVER -> 0xFFD0D0D0;
        };
    }
}
