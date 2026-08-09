package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.particle.ModParticles;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;

/**
 * 肥皂入水粒子：仅在<strong>肥皂底部</strong>生成；深度测试开启。
 * 泡泡效果期亦复用同款精灵粒子（环绕实体）。
 */
public final class SoapWaterParticles {

    /**
     * 肥皂底部粒子高度（方块局部 Y）。完整皂碰撞约 {@code y=0..2/16}，略抬离地面以免扎进方块。
     */
    private static final double SOAP_BOTTOM_Y = 0.04;

    private SoapWaterParticles() {}

    /** 浸水期间少量持续效果（仅服务端同步）。 */
    public static void spawnAmbient(ServerLevel level, BlockPos pos, BlockState state, SoapBarBlockEntity be) {
        if (level.random.nextInt(3) != 0) {
            return;
        }
        int particleMatId = resolveParticleMat(state, be);
        SimpleParticleType type = ModParticles.soapDissolve(particleMatId);
        double y = pos.getY() + SOAP_BOTTOM_Y + level.random.nextDouble() * 0.04;
        spawnOne(level, type, pos, y, 0.01, 0.02);
    }

    /**
     * 泡泡效果期：环绕实体喷少量入水溶解粒子（色由 {@code particleMatId} 决定，与入水皂同源）。
     */
    public static void spawnAmbientAroundEntity(
            ServerLevel level, LivingEntity entity, int particleMatId) {
        if (level.random.nextInt(3) != 0) {
            return;
        }
        SimpleParticleType type = ModParticles.soapDissolve(particleMatId);
        double width = Math.max(0.4, entity.getBbWidth());
        double height = Math.max(0.6, entity.getBbHeight());
        double x = entity.getX() + (level.random.nextDouble() - 0.5) * width;
        double y = entity.getY() + level.random.nextDouble() * height;
        double z = entity.getZ() + (level.random.nextDouble() - 0.5) * width;
        double vy = 0.01 + level.random.nextDouble() * 0.02;
        send(level, type, x, y, z, 0.0, vy, 0.0);
    }

    /** 耐久消耗推进或消失时的短爆发。 */
    public static void spawnBurst(ServerLevel level, BlockPos pos, BlockState state, SoapBarBlockEntity be) {
        int particleMatId = resolveParticleMat(state, be);
        SimpleParticleType type = ModParticles.soapDissolve(particleMatId);
        double baseY = pos.getY() + SOAP_BOTTOM_Y;
        int count = 3 + level.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            burstOne(level, type, pos, baseY + level.random.nextDouble() * 0.05);
        }
    }

    private static void spawnOne(
            ServerLevel level,
            SimpleParticleType type,
            BlockPos soapPos,
            double y,
            double vyMin,
            double vySpan) {
        double x = soapPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
        double z = soapPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
        double vy = vyMin + level.random.nextDouble() * vySpan;
        send(level, type, x, y, z, 0.0, vy, 0.0);
    }

    private static void burstOne(
            ServerLevel level, SimpleParticleType type, BlockPos soapPos, double y) {
        double x = soapPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
        double z = soapPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
        double vx = (level.random.nextDouble() - 0.5) * 0.03;
        double vy = 0.02 + level.random.nextDouble() * 0.05;
        double vz = (level.random.nextDouble() - 0.5) * 0.03;
        send(level, type, x, y, z, vx, vy, vz);
    }

    static int resolveParticleMat(BlockState state, SoapBarBlockEntity be) {
        if (!(state.getBlock() instanceof SoapBarBlock block)) {
            return SoapBarAppearance.DEFAULT_PARTICLE_MAT;
        }
        if (be.particleFromLiquid() && be.particleMatId() > 0) {
            return be.particleMatId();
        }
        int fromPigment =
                SoapBarAppearance.pigmentToParticleMat(state.getValue(block.MATERIAL));
        if (be.particleMatId() != fromPigment || be.particleFromLiquid()) {
            be.setParticleMat(fromPigment, false);
        }
        return fromPigment;
    }

    private static void send(
            ServerLevel level,
            SimpleParticleType type,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz) {
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, type, true, x, y, z, 0, vx, vy, vz, 1.0);
        }
    }
}
