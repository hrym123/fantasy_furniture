package org.lanye.fantasy_furniture.content.soap.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.lanye.fantasy_furniture.bootstrap.effect.ModEffects;

/**
 * 施加泡泡效果：无 → Ⅰ（兔耳）；Ⅰ → Ⅱ（猫耳）；Ⅱ → Ⅰ。
 *
 * <p>原版 {@link LivingEntity#addEffect} 不会用更低 amplifier 覆盖更高档，故切换时先
 * {@link LivingEntity#removeEffect} 再施加。时长在剩余时间上叠加，上限
 * {@link BubbleMobEffect#MAX_DURATION_TICKS}。
 */
public final class BubbleEffectApplier {

    private BubbleEffectApplier() {}

    /** 站立长按吹泡：+{@link BubbleMobEffect#DURATION_TICKS}，不消耗肥皂。 */
    public static void apply(LivingEntity entity) {
        applyWithExtraDuration(entity, BubbleMobEffect.DURATION_TICKS);
    }

    /** 蹲下长按消耗肥皂：+{@link BubbleMobEffect#CONSUME_DURATION_TICKS}。 */
    public static void applyFromConsumedSoap(LivingEntity entity) {
        applyWithExtraDuration(entity, BubbleMobEffect.CONSUME_DURATION_TICKS);
    }

    private static void applyWithExtraDuration(LivingEntity entity, int extraTicks) {
        MobEffectInstance current = entity.getEffect(ModEffects.BUBBLE.get());
        int remaining = current != null ? current.getDuration() : 0;
        int variant = BubbleMobEffect.VARIANT_RABBIT;
        if (current != null && current.getAmplifier() == BubbleMobEffect.VARIANT_RABBIT) {
            variant = BubbleMobEffect.VARIANT_CAT;
        }
        int duration = Math.min(remaining + extraTicks, BubbleMobEffect.MAX_DURATION_TICKS);
        if (current != null) {
            entity.removeEffect(ModEffects.BUBBLE.get());
        }
        entity.addEffect(
                new MobEffectInstance(
                        ModEffects.BUBBLE.get(),
                        duration,
                        variant,
                        false,
                        true,
                        true));
    }
}
