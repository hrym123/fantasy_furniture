package org.lanye.fantasy_furniture.content.furniture.decor.series;

import java.util.function.Consumer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

@OnlyIn(Dist.CLIENT)
public final class StyledWindowSeriesBlockClientExtensions {

    private StyledWindowSeriesBlockClientExtensions() {}

    public static void register(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(
                new IClientBlockExtensions() {
                    @Override
                    public boolean addDestroyEffects(
                            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                        StyledWindowSeriesBreakParticles.spawn(state, level, pos, manager);
                        return true;
                    }
                });
    }
}
