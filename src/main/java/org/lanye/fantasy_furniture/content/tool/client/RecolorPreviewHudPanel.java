package org.lanye.fantasy_furniture.content.tool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.tool.BrushRecolorPreview;

/** 刷子 / 线轴换色预览 HUD 共用面板绘制（同框同布局）。 */
@OnlyIn(Dist.CLIENT)
final class RecolorPreviewHudPanel {

    static final ResourceLocation FRAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "textures/gui/paint_brush_recolor_preview.png");

    private static final int FRAME_TEX_WIDTH = 32;
    private static final int FRAME_TEX_HEIGHT = 28;
    private static final int FRAME_BORDER = 4;
    private static final int FRAME_CENTER_W = FRAME_TEX_WIDTH - FRAME_BORDER * 2;
    private static final int ICON_SIZE = 16;
    private static final int INNER_PAD_X = 3;
    private static final int GAP_ABOVE_STATUS = 6;
    private static final float HINT_SCALE = 0.85f;
    private static final int HINT_COLOR = 0xC8C8C8;
    private static final int HINT_GAP = 2;

    private RecolorPreviewHudPanel() {}

    static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            ItemStack stack,
            Component name) {
        Font font = Minecraft.getInstance().font;
        String hint = Component.translatable("hud.fantasy_furniture.paint_brush_recolor_preview").getString();
        String display = BrushRecolorPreview.truncateDisplayName(name.getString());
        int hintWidth = (int) (font.width(hint) * HINT_SCALE);
        int hintHeight = (int) (font.lineHeight * HINT_SCALE);
        int textWidth = font.width(display);
        int contentTextWidth = Math.max(hintWidth, textWidth);
        int innerWidth = INNER_PAD_X + ICON_SIZE + INNER_PAD_X + contentTextWidth + INNER_PAD_X;
        int panelWidth = innerWidth + FRAME_BORDER * 2;
        int panelHeight = FRAME_TEX_HEIGHT;

        int x = (screenWidth - panelWidth) / 2;
        int statusTop = screenHeight - Math.max(gui.leftHeight, gui.rightHeight) - 39;
        int y = statusTop - panelHeight - GAP_ABOVE_STATUS;

        drawFrameBorder(graphics, x, y, panelWidth, panelHeight);

        int innerHeight = panelHeight - FRAME_BORDER * 2;
        int iconX = x + FRAME_BORDER + INNER_PAD_X;
        int iconY = y + FRAME_BORDER + (innerHeight - ICON_SIZE) / 2;
        graphics.renderItem(stack, iconX, iconY);
        graphics.renderItemDecorations(font, stack, iconX, iconY);

        int textX = iconX + ICON_SIZE + INNER_PAD_X;
        int blockHeight = hintHeight + HINT_GAP + font.lineHeight;
        int blockTop = y + FRAME_BORDER + (innerHeight - blockHeight) / 2;
        int hintY = blockTop;
        int textY = blockTop + hintHeight + HINT_GAP;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(textX, hintY, 0);
        pose.scale(HINT_SCALE, HINT_SCALE, 1f);
        graphics.drawString(font, hint, 0, 0, HINT_COLOR, true);
        pose.popPose();

        graphics.drawString(font, display, textX, textY, 0xFFFFFF, true);
    }

    private static void drawFrameBorder(GuiGraphics graphics, int x, int y, int panelWidth, int panelHeight) {
        int centerDstW = panelWidth - FRAME_BORDER * 2;
        int tw = FRAME_TEX_WIDTH;
        int th = FRAME_TEX_HEIGHT;

        graphics.blit(FRAME_TEXTURE, x, y, FRAME_BORDER, panelHeight, 0, 0, FRAME_BORDER, th, tw, th);
        if (centerDstW > 0) {
            int dstX = x + FRAME_BORDER;
            int remaining = centerDstW;
            while (remaining > 0) {
                int tileW = Math.min(remaining, FRAME_CENTER_W);
                graphics.blit(
                        FRAME_TEXTURE, dstX, y, tileW, panelHeight, FRAME_BORDER, 0, tileW, th, tw, th);
                dstX += tileW;
                remaining -= tileW;
            }
        }
        graphics.blit(
                FRAME_TEXTURE,
                x + panelWidth - FRAME_BORDER,
                y,
                FRAME_BORDER,
                panelHeight,
                FRAME_TEX_WIDTH - FRAME_BORDER,
                0,
                FRAME_BORDER,
                th,
                tw,
                th);
    }
}
