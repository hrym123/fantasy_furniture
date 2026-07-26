package org.lanye.fantasy_furniture.content.tool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.tool.CoilRecolorPreview;
import org.lanye.reverie_core.bootstrap.item.ModItems;

@OnlyIn(Dist.CLIENT)
public final class FantasySpoolRecolorHudOverlay implements IGuiOverlay {

    private static final FantasySpoolRecolorHudOverlay INSTANCE = new FantasySpoolRecolorHudOverlay();

    private FantasySpoolRecolorHudOverlay() {}

    static FantasySpoolRecolorHudOverlay instance() {
        return INSTANCE;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (!mc.player.getMainHandItem().is(ModItems.FANTASY_SPOOL.get())) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if (!state.is(ModTags.COIL_RECOLORABLE_BLOCKS)) {
            return;
        }
        var previewOpt =
                CoilRecolorPreview.forHit(mc.level, state, blockHit.getBlockPos(), blockHit.getLocation());
        if (previewOpt.isEmpty()) {
            return;
        }
        CoilRecolorPreview.Preview preview = previewOpt.get();
        RecolorPreviewHudPanel.render(gui, graphics, screenWidth, screenHeight, preview.stack(), preview.name());
    }
}
