package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 肥皂入水粒子：精灵表切帧（moonstarfish {@code 粒子_*.png}）；每 1 秒切 2 帧。
 */
@OnlyIn(Dist.CLIENT)
public final class SoapDissolveParticle extends TextureSheetParticle {

    /** 与 {@code particles/soap_dissolve_*.json} 的 textures 条数一致（两列 × 16）。 */
    private static final int FRAME_COUNT = 32;

    /** 10 tick = 0.5 秒 → 1 秒切 2 帧。 */
    private static final int TICKS_PER_FRAME = 10;

    private final SpriteSet sprites;

    private SoapDissolveParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.gravity = -0.015f;
        this.friction = 0.9f;
        this.lifetime = FRAME_COUNT * TICKS_PER_FRAME;
        this.quadSize = 0.14f + this.random.nextFloat() * 0.08f;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.hasPhysics = false;
        setSprite(sprites.get(0, FRAME_COUNT));
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            int frame = Math.min(this.age / TICKS_PER_FRAME, FRAME_COUNT - 1);
            setSprite(sprites.get(frame, FRAME_COUNT));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed) {
            return new SoapDissolveParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
