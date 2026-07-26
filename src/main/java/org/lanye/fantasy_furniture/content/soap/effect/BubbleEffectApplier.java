package org.lanye.fantasy_furniture.content.soap.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.lanye.fantasy_furniture.bootstrap.effect.ModEffects;

/** 施加泡泡效果（随机兔耳 / 猫耳 amplifier）。 */
public final class BubbleEffectApplier {

    private BubbleEffectApplier() {}

    public static void apply(LivingEntity entity) {
        int variant =
                entity.getRandom().nextBoolean()
                        ? BubbleMobEffect.VARIANT_CAT
                        : BubbleMobEffect.VARIANT_RABBIT;
        entity.addEffect(
                new MobEffectInstance(
                        ModEffects.BUBBLE.get(),
                        BubbleMobEffect.DURATION_TICKS,
                        variant,
                        false,
                        true,
                        true));
    }
}
