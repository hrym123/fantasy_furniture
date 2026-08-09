package org.lanye.fantasy_furniture.content.soap.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.lanye.fantasy_furniture.bootstrap.effect.ModEffects;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidMaterials;

/**
 * 施加泡泡效果：无 → Ⅰ（兔耳）；Ⅰ → Ⅱ（猫耳）；Ⅱ → Ⅰ。
 *
 * <p>原版 {@link LivingEntity#addEffect} 不会用更低 amplifier 覆盖更高档，故切换时先
 * {@link LivingEntity#removeEffect} 再施加。时长在剩余时间上叠加，上限
 * {@link BubbleMobEffect#MAX_DURATION_TICKS}。
 *
 * <p>身体粒子色取自吹泡所用肥皂的 {@link SoapBarAppearance#particleMatId()}（与入水溶解粒子同源）；
 * 原版效果螺旋粒子关闭（{@code visible=false}），改由 {@link BubbleMobEffect} 刷自定义粒子。
 */
public final class BubbleEffectApplier {

    /** 实体持久数据：泡泡效果身体粒子材质 id（1–6）。 */
    public static final String TAG_BUBBLE_PART_MAT = "FantasyFurnitureBubblePartMat";

    private BubbleEffectApplier() {}

    /** 站立长按吹泡：+{@link BubbleMobEffect#DURATION_TICKS}，不消耗肥皂。 */
    public static void apply(LivingEntity entity, SoapBarAppearance appearance) {
        applyWithExtraDuration(entity, BubbleMobEffect.DURATION_TICKS, resolveParticleMat(appearance));
    }

    /** 蹲下长按消耗肥皂：+{@link BubbleMobEffect#CONSUME_DURATION_TICKS}。 */
    public static void applyFromConsumedSoap(LivingEntity entity, SoapBarAppearance appearance) {
        applyWithExtraDuration(
                entity, BubbleMobEffect.CONSUME_DURATION_TICKS, resolveParticleMat(appearance));
    }

    private static void applyWithExtraDuration(
            LivingEntity entity, int extraTicks, int particleMatId) {
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
        entity.getPersistentData().putInt(TAG_BUBBLE_PART_MAT, particleMatId);
        // ambient=false, visible=false（关原版螺旋色粒子）, showIcon=true
        entity.addEffect(
                new MobEffectInstance(
                        ModEffects.BUBBLE.get(),
                        duration,
                        variant,
                        false,
                        false,
                        true));
    }

    static int resolveParticleMat(SoapBarAppearance appearance) {
        int part = appearance.particleMatId();
        if (SoapFlatLiquidMaterials.isValid(part)) {
            return part;
        }
        return SoapBarAppearance.pigmentToParticleMat(appearance.materialId());
    }

    static int readParticleMat(LivingEntity entity) {
        int part = entity.getPersistentData().getInt(TAG_BUBBLE_PART_MAT);
        if (SoapFlatLiquidMaterials.isValid(part)) {
            return part;
        }
        return SoapBarAppearance.DEFAULT_PARTICLE_MAT;
    }

    static void clearParticleMat(LivingEntity entity) {
        entity.getPersistentData().remove(TAG_BUBBLE_PART_MAT);
    }
}
