package org.lanye.fantasy_furniture.content.furniture.decor.client;

import java.util.function.Consumer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.lanye.fantasy_furniture.content.furniture.decor.block.StyledWindow0Block;

@OnlyIn(Dist.CLIENT)
public final class StyledWindow0BlockClientExtensions implements IClientBlockExtensions {

    public static final StyledWindow0BlockClientExtensions INSTANCE =
            new StyledWindow0BlockClientExtensions();

    private StyledWindow0BlockClientExtensions() {}

    public static void register(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @Override
    public boolean addDestroyEffects(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (state.getBlock() instanceof StyledWindow0Block) {
            StyledWindow0BreakParticles.spawn(state, level, pos, manager);
            return true;
        }
        return false;
    }

    @Override
    public boolean addHitEffects(
            BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (state.getBlock() instanceof StyledWindow0Block
                && target instanceof BlockHitResult blockHit) {
            StyledWindow0BreakParticles.spawn(state, level, blockHit.getBlockPos(), manager);
            return true;
        }
        return false;
    }
}
