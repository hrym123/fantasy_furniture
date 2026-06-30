package org.lanye.fantasy_furniture.content.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.reverie_core.tool.PaintBrushRecolorHandler;
import org.lanye.reverie_core.tool.PaintBrushRecolorHandlers;

/** 向 reverie_core 注册刷子换色逻辑（{#fantasy_furniture:brush_recolorable}）。 */
public final class FantasyFurniturePaintBrushRegistration {

    private FantasyFurniturePaintBrushRegistration() {}

    public static void register() {
        PaintBrushRecolorHandlers.register(
                new PaintBrushRecolorHandler() {
                    @Override
                    public boolean isRecolorableBlock(BlockState state) {
                        return state.is(ModTags.BRUSH_RECOLORABLE_BLOCKS);
                    }

                    @Override
                    public boolean applyRecolor(Level level, BlockPos pos, BlockState state) {
                        return BrushRecolor.apply(level, pos, state);
                    }
                });
    }
}
