package org.lanye.fantasy_furniture.content.soap.effect;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.SoapWaterParticles;

/**
 * 泡泡效果：持续向上漂浮（对齐原版漂浮手感）、按入水方式耗氧；头饰造型由 amplifier
 * 区分（0 兔耳 / 1 猫耳）。牛奶等原版治愈物可清除。
 *
 * <p>原版漂浮在 {@code LivingEntity#travel} 内改竖直速度并绕过普通下落重力；自定义效果若只在
 * {@link #applyEffectTick} 里 {@code +0.05}，随后仍吃重力（约 {@code -0.08}），观感会像缓降。
 * 此处取消实体重力并套用与原版漂浮Ⅰ相同的速度插值。
 *
 * <p>身体粒子：关闭原版效果螺旋，改刷与入水肥皂同款的溶解精灵（色见
 * {@link BubbleEffectApplier#TAG_BUBBLE_PART_MAT}）。
 */
public final class BubbleMobEffect extends MobEffect {

    /** 单次吹泡叠加时长：10 秒（不消耗）。 */
    public static final int DURATION_TICKS = 20 * 10;

    /** 蹲下长按消耗肥皂：+10 分钟。 */
    public static final int CONSUME_DURATION_TICKS = 20 * 60 * 10;

    /** 肥皂叠加上限：1 小时。 */
    public static final int MAX_DURATION_TICKS = 20 * 60 * 60;

    /** 长按充能约 1.5 秒。 */
    public static final int USE_TICKS = 30;

    public static final int VARIANT_RABBIT = 0;
    public static final int VARIANT_CAT = 1;

    /** 对齐原版漂浮Ⅰ目标竖直速度；amplifier 专用于兔/猫耳，不参与抬升倍率。 */
    private static final double FLOAT_TARGET_Y = 0.05D;

    private static final double FLOAT_BLEND = 0.2D;

    /** 与 Forge 默认实体重力 0.08 相消。 */
    private static final UUID ANTI_GRAVITY_ID =
            UUID.fromString("6e8f0c2a-4b5d-4e1f-9c3a-7d2e1f0a9b8c");

    private static final AttributeModifier ANTI_GRAVITY =
            new AttributeModifier(
                    ANTI_GRAVITY_ID,
                    "fantasy_furniture.bubble_float",
                    -0.08D,
                    AttributeModifier.Operation.ADDITION);

    public BubbleMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0x7EC8E3);
    }

    @Override
    public void addAttributeModifiers(
            @NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        ensureAntiGravity(entity);
    }

    @Override
    public void removeAttributeModifiers(
            @NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity != null) {
            gravity.removeModifier(ANTI_GRAVITY_ID);
        }
        BubbleEffectApplier.clearParticleMat(entity);
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        ensureAntiGravity(entity);

        if (!(entity instanceof Player player && player.getAbilities().flying)) {
            // 与 LivingEntity.travel 中原版漂浮分支同式：y += (0.05*(amp+1) - y) * 0.2
            Vec3 delta = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    delta.x, delta.y + (FLOAT_TARGET_Y - delta.y) * FLOAT_BLEND, delta.z);
            entity.resetFallDistance();
            entity.hasImpulse = true;
        }

        if (entity.level().isClientSide) {
            return;
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            SoapWaterParticles.spawnAmbientAroundEntity(
                    serverLevel, entity, BubbleEffectApplier.readParticleMat(entity));
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

    private static void ensureAntiGravity(LivingEntity entity) {
        AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity != null && !gravity.hasModifier(ANTI_GRAVITY)) {
            gravity.addTransientModifier(ANTI_GRAVITY);
        }
    }
}
