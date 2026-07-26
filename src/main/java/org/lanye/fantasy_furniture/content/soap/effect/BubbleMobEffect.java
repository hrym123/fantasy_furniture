package org.lanye.fantasy_furniture.content.soap.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 泡泡效果：持续漂浮、按入水方式耗氧；头饰造型由 {@link net.minecraft.world.effect.MobEffectInstance#getAmplifier()}
 * 区分（0 兔耳 / 1 猫耳）。牛奶等原版治愈物可清除。
 */
public final class BubbleMobEffect extends MobEffect {

    /** 设计：10 秒。 */
    public static final int DURATION_TICKS = 20 * 10;

    /** 长按充能约 1.5 秒。 */
    public static final int USE_TICKS = 30;

    public static final int VARIANT_RABBIT = 0;
    public static final int VARIANT_CAT = 1;

    public BubbleMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0x7EC8E3);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 漂浮（对齐原版漂浮感，略弱）
        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.05D, 0.0D));
        entity.fallDistance = 0.0F;
        entity.hasImpulse = true;

        if (entity.level().isClientSide) {
            return;
        }

        // baseTick 已在非水中回气约 +4；再扣 5 → 净约 -1 / tick，贴近入水耗氧
        int air = entity.getAirSupply() - 5;
        entity.setAirSupply(air);
        if (air <= -20) {
            entity.setAirSupply(0);
            entity.hurt(entity.damageSources().drown(), 2.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
