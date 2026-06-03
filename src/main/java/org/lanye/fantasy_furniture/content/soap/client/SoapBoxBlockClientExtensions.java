package org.lanye.fantasy_furniture.content.soap.client;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;

@OnlyIn(Dist.CLIENT)
public final class SoapBoxBlockClientExtensions implements IClientBlockExtensions {

    public static final SoapBoxBlockClientExtensions INSTANCE = new SoapBoxBlockClientExtensions();

    private SoapBoxBlockClientExtensions() {}

    public static void register(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @Override
    public boolean addDestroyEffects(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (state.getBlock() instanceof SoapBoxBlock) {
            SoapGeoBreakParticles.forSoapBox(state, level, pos, manager);
            return true;
        }
        return false;
    }

    @Override
    public boolean addHitEffects(
            BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (state.getBlock() instanceof SoapBoxBlock
                && target instanceof BlockHitResult blockHit) {
            SoapGeoBreakParticles.forSoapBox(state, level, blockHit.getBlockPos(), manager);
            return true;
        }
        return false;
    }
}
