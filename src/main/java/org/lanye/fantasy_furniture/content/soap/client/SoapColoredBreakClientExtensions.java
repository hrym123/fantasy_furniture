package org.lanye.fantasy_furniture.content.soap.client;

import java.util.function.Consumer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;
import org.lanye.fantasy_furniture.content.soap.block.DisplayCabinetBlock;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;

/**
 * 多材质档肥皂系方块：覆盖默认模型粒子（JSON 写死 {@code *_1}/{@code *_2}），按状态材质喷色。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapColoredBreakClientExtensions implements IClientBlockExtensions {

    public static final SoapColoredBreakClientExtensions INSTANCE =
            new SoapColoredBreakClientExtensions();

    private SoapColoredBreakClientExtensions() {}

    public static void register(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(INSTANCE);
    }

    @Override
    public boolean addDestroyEffects(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return spawn(state, level, pos, manager);
    }

    @Override
    public boolean addHitEffects(
            BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (target instanceof BlockHitResult blockHit) {
            return spawn(state, level, blockHit.getBlockPos(), manager);
        }
        return false;
    }

    private static boolean spawn(
            BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        Block block = state.getBlock();
        if (block instanceof SoapBarBlock) {
            SoapGeoBreakParticles.forSoapBar(state, level, pos, manager);
            return true;
        }
        if (block instanceof SoapPaperBagBlock) {
            SoapGeoBreakParticles.forSoapPaperBag(state, level, pos, manager);
            return true;
        }
        if (block instanceof BodyWashBlock) {
            SoapGeoBreakParticles.forBodyWash(state, level, pos, manager);
            return true;
        }
        if (block instanceof ShampooBlock) {
            SoapGeoBreakParticles.forShampoo(state, level, pos, manager);
            return true;
        }
        if (block instanceof BodyCreamBlock) {
            SoapGeoBreakParticles.forBodyCream(state, level, pos, manager);
            return true;
        }
        if (block instanceof DisplayCabinetBlock) {
            SoapGeoBreakParticles.forDisplayCabinet(state, level, pos, manager);
            return true;
        }
        return false;
    }
}
