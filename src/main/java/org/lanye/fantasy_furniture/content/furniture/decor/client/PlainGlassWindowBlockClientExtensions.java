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
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;

@OnlyIn(Dist.CLIENT)
public final class PlainGlassWindowBlockClientExtensions implements IClientBlockExtensions {

    public static final PlainGlassWindowBlockClientExtensions INSTANCE =
            new PlainGlassWindowBlockClientExtensions();

    private PlainGlassWindowBlockClientExtensions() {}

    public static void register(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @Override
    public boolean addDestroyEffects(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (state.getBlock() instanceof PlainGlassWindowBlock) {
            PlainGlassWindowBreakParticles.spawn(state, level, pos, manager);
            return true;
        }
        return false;
    }

    @Override
    public boolean addHitEffects(
            BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (state.getBlock() instanceof PlainGlassWindowBlock
                && target instanceof BlockHitResult blockHit) {
            PlainGlassWindowBreakParticles.spawn(state, level, blockHit.getBlockPos(), manager);
            return true;
        }
        return false;
    }
}
