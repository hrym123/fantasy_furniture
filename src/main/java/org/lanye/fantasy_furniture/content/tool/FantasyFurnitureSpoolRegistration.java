package org.lanye.fantasy_furniture.content.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.reverie_core.tool.SpoolRecolorHandler;
import org.lanye.reverie_core.tool.SpoolRecolorHandlers;

/** 向 reverie_core 注册线轴换色逻辑（{#fantasy_furniture:coil_recolorable}）。 */
public final class FantasyFurnitureSpoolRegistration {

    private FantasyFurnitureSpoolRegistration() {}

    public static void register() {
        SpoolRecolorHandlers.register(
                new SpoolRecolorHandler() {
                    @Override
                    public boolean isRecolorableBlock(BlockState state) {
                        return state.is(ModTags.COIL_RECOLORABLE_BLOCKS);
                    }

                    @Override
                    public boolean applyRecolor(
                            Level level, BlockPos pos, BlockState state, Vec3 hitLocation) {
                        return CoilRecolor.apply(level, state, pos, hitLocation);
                    }
                });
    }
}
