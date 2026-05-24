package org.lanye.fantasy_furniture.content.tool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.tool.BrushRecolorPreview;

@OnlyIn(Dist.CLIENT)
public final class PaintBrushRecolorHudOverlay implements IGuiOverlay {

    static final ResourceLocation FRAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "textures/gui/paint_brush_recolor_preview.png");

    private static final int FRAME_TEX_WIDTH = 32;
    /** 与面板高度一致。 */
    private static final int FRAME_TEX_HEIGHT = 28;
    private static final int FRAME_BORDER = 4;
    private static final int FRAME_CENTER_W = FRAME_TEX_WIDTH - FRAME_BORDER * 2;
    private static final int ICON_SIZE = 16;
    private static final int INNER_PAD_X = 3;
    private static final int GAP_ABOVE_STATUS = 6;

    private static final PaintBrushRecolorHudOverlay INSTANCE = new PaintBrushRecolorHudOverlay();

    private PaintBrushRecolorHudOverlay() {}

    static PaintBrushRecolorHudOverlay instance() {
        return INSTANCE;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!mc.player.getMainHandItem().is(ModItems.PAINT_BRUSH.get())) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if (!state.is(ModTags.BRUSH_RECOLORABLE_BLOCKS)) {
            return;
        }
        var previewOpt = BrushRecolorPreview.forTargetState(state);
        if (previewOpt.isEmpty()) {
            return;
        }
        BrushRecolorPreview.Preview preview = previewOpt.get();
        renderPanel(gui, graphics, screenWidth, screenHeight, preview.stack(), preview.name());
    }

    private static void renderPanel(
            ForgeGui gui,
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            ItemStack stack,
            Component name) {
        Font font = Minecraft.getInstance().font;
        String display = BrushRecolorPreview.truncateDisplayName(name.getString());
        int textWidth = font.width(display);
        int innerWidth = INNER_PAD_X + ICON_SIZE + INNER_PAD_X + textWidth + INNER_PAD_X;
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
        int textY = y + FRAME_BORDER + (innerHeight - font.lineHeight) / 2 + 1;
        graphics.drawString(font, display, textX, textY, 0xFFFFFF, true);
    }

    /**
     * 横向三切片边框。不用 {@link GuiGraphics#blitNineSliced}：其内部 {@code blit(u,v,w,h)} 固定按 256×256 算 UV，
     * 非 256 图集贴图只会采到顶部窄条。
     */
    private static void drawFrameBorder(GuiGraphics graphics, int x, int y, int panelWidth, int panelHeight) {
        int centerDstW = panelWidth - FRAME_BORDER * 2;
        int tw = FRAME_TEX_WIDTH;
        int th = FRAME_TEX_HEIGHT;

        graphics.blit(
                FRAME_TEXTURE,
                x,
                y,
                FRAME_BORDER,
                panelHeight,
                0,
                0,
                FRAME_BORDER,
                th,
                tw,
                th);
        if (centerDstW > 0) {
            graphics.blit(
                    FRAME_TEXTURE,
                    x + FRAME_BORDER,
                    y,
                    centerDstW,
                    panelHeight,
                    FRAME_BORDER,
                    0,
                    FRAME_CENTER_W,
                    th,
                    tw,
                    th);
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
