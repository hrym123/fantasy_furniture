package org.lanye.fantasy_furniture.content.furniture.common.item;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.common.client.renderer.ArcaneWandGeoItemRenderer;
import org.lanye.fantasy_furniture.core.geolib.GeolibHandheldItem;
import org.lanye.fantasy_furniture.core.geolib.GeolibItemAssets;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

/**
 * 魔杖：长按开始播 cast1；cast1 结束后无缝进入循环 cast2；松手后不立刻 cast3，而是等<strong>当前这一轮</strong> cast2
 * 播完（若在 cast1 松手则先播完剩余 cast1，再完整播一轮 cast2），再无缝播 cast3，最后 idle。阶段由 ItemStack NBT 驱动；
 * GeckoLib 仅在阶段变化时 {@code setAnimation}，避免每帧重置循环导致「又从头播一遍动画2」。
 */
public class ArcaneWandItem extends GeolibHandheldItem {

    /** cast1 时长 0.5s → 10 tick @ 20Hz */
    static final int CAST1_TICKS = 10;

    /** cast2 单次循环长度，与 {@code arcane_wand.animation.json} 中 {@code animation.arcane_wand.cast2} 的 animation_length（秒）×20 一致 */
    static final int CAST2_LOOP_TICKS = 20;

    /** cast3 时长 0.5s → 10 tick @ 20Hz，与 arcane_wand.animation.json 中 cast3 的 animation_length 一致 */
    static final int CAST3_TICKS = 10;

    static final String TAG_ANIM = "FantasyWandAnim";
    static final String TAG_CAST3_LEFT = "FantasyWandCast3Left";

    /** 松手后距离开始播放 cast3 的剩余 tick；等待期间保持 anim=1 或 2 */
    static final String TAG_CAST3_DELAY = "FantasyWandCast3Delay";

    /**
     * 记录每个 AnimationController 上次已 set 的阶段。不能依赖 ItemStack NBT（渲染回调拿到的栈实例可能不是稳定同一对象），
     * 否则会出现 last=-1 且每帧重复 setAnimation 的抖动。
     */
    private static final Map<AnimationController<?>, Integer> LAST_ANIM_BY_CONTROLLER =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final RawAnimation CAST1 =
            RawAnimation.begin().then("animation.arcane_wand.cast1", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation CAST2 =
            RawAnimation.begin().then("animation.arcane_wand.cast2", Animation.LoopType.LOOP);
    private static final RawAnimation CAST3 =
            RawAnimation.begin().then("animation.arcane_wand.cast3", Animation.LoopType.PLAY_ONCE);

    /** 施法段之间切换时的过渡（tick）；略短以免拖泥带水 */
    private static final int TRANSITION_CAST_TICKS = 6;

    /**
     * cast3 结束接 idle：末帧与 idle 首帧姿态差大，需更长混合；见 {@link AnimationController#transitionLength(int)}。
     */
    private static final int TRANSITION_CAST3_TO_IDLE_TICKS = 12;

    /** 其它情况回到 idle（如空栈） */
    private static final int TRANSITION_TO_IDLE_TICKS = 6;

    public ArcaneWandItem(Properties properties, GeolibItemAssets assets, String idleAnimation) {
        super(properties, assets, idleAnimation);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return ArcaneWandGeoItemRenderer.INSTANCE;
                    }
                });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(TAG_CAST3_DELAY);
        tag.remove(TAG_CAST3_LEFT);
        tag.putByte(TAG_ANIM, (byte) 1);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public void onUseTick(
            @NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!stack.is(this)) {
            return;
        }
        int ticksUsing = getUseDuration(stack) - remainingUseDuration;
        CompoundTag tag = stack.getOrCreateTag();
        byte current = tag.getByte(TAG_ANIM);
        // 松手后阶段由 releaseUsing / inventoryTick 驱动；若仍误触发本方法，不能把 cast3(3) 写回 2
        if (tag.contains(TAG_CAST3_DELAY) || current == 3 || tag.getInt(TAG_CAST3_LEFT) > 0) {
            return;
        }
        // 一旦进入 cast2，本次长按期间不再回退到 cast1，消除边界 tick 的 1<->2 抖动
        byte next = current == 2 ? 2 : (byte) (ticksUsing < CAST1_TICKS ? 1 : 2);
        if (tag.getByte(TAG_ANIM) != next) {
            tag.putByte(TAG_ANIM, next);
        }
    }

    /**
     * 需在客户端与逻辑服各执行一次：松手当帧客户端栈必须立刻带上 delay/anim。
     * delay / cast3 剩余在客户端与服务器两侧用相同规则递减，避免渲染栈长期滞后于逻辑服。
     */
    @Override
    public void releaseUsing(
            @NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeCharged) {
        if (!stack.is(this)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        // 参数 timeCharged 实际是剩余使用时长（timeLeft），需换算为已蓄力 tick
        int heldTicks = getUseDuration(stack) - timeCharged;
        int delay;
        byte anim;
        if (heldTicks < CAST1_TICKS) {
            delay = (CAST1_TICKS - heldTicks) + CAST2_LOOP_TICKS;
            anim = 1;
        } else {
            int ticksInCast2 = heldTicks - CAST1_TICKS;
            int phase = Math.floorMod(ticksInCast2, CAST2_LOOP_TICKS);
            delay = CAST2_LOOP_TICKS - phase;
            if (delay == 0) {
                delay = CAST2_LOOP_TICKS;
            }
            anim = 2;
        }
        tag.putInt(TAG_CAST3_DELAY, delay);
        tag.putByte(TAG_ANIM, anim);
    }

    @Override
    public void inventoryTick(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull net.minecraft.world.entity.Entity entity,
            int slotId,
            boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        if (!stack.is(this)) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        int cast3Delay = tag.getInt(TAG_CAST3_DELAY);
        if (cast3Delay > 0) {
            int nextDelay = cast3Delay - 1;
            tag.putInt(TAG_CAST3_DELAY, nextDelay);
            if (tag.getByte(TAG_ANIM) == 1 && nextDelay == CAST2_LOOP_TICKS) {
                tag.putByte(TAG_ANIM, (byte) 2);
            }
            if (nextDelay == 0) {
                tag.remove(TAG_CAST3_DELAY);
                tag.putByte(TAG_ANIM, (byte) 3);
                tag.putInt(TAG_CAST3_LEFT, CAST3_TICKS);
            }
            return;
        }
        if (tag.getByte(TAG_ANIM) == 3) {
            int left = tag.getInt(TAG_CAST3_LEFT);
            if (left > 0) {
                tag.putInt(TAG_CAST3_LEFT, left - 1);
            } else {
                tag.putByte(TAG_ANIM, (byte) 0);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        FantasyFurniture.MODID + ":wand_cast",
                        TRANSITION_CAST_TICKS,
                        (AnimationState<GeolibHandheldItem> state) -> {
                            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
                            if (stack == null || stack.isEmpty()) {
                                state.getController()
                                        .transitionLength(TRANSITION_TO_IDLE_TICKS)
                                        .setAnimation(idleAnimation());
                                return PlayState.CONTINUE;
                            }
                            CompoundTag tag = stack.getTag();
                            int anim = tag != null ? tag.getByte(TAG_ANIM) : 0;
                            AnimationController<?> controller = state.getController();
                            int lastAnim = LAST_ANIM_BY_CONTROLLER.getOrDefault(controller, -1);
                            // 副本间 NBT 短暂不一致时，禁止「已从 cast3 退下」的栈把阶段读回 cast2；仅当上一帧已是 idle 时才吞掉「凭空出现的 3」
                            if (lastAnim == 3 && anim == 2) {
                                anim = 3;
                            } else if (lastAnim == 0 && anim == 3) {
                                anim = 0;
                            }
                            if (lastAnim == anim) {
                                return PlayState.CONTINUE;
                            }
                            switch (anim) {
                                case 1 -> controller
                                        .transitionLength(TRANSITION_CAST_TICKS)
                                        .setAnimation(CAST1);
                                case 2 -> controller
                                        .transitionLength(TRANSITION_CAST_TICKS)
                                        .setAnimation(CAST2);
                                case 3 -> controller
                                        .transitionLength(TRANSITION_CAST_TICKS)
                                        .setAnimation(CAST3);
                                default -> controller
                                        .transitionLength(
                                                lastAnim == 3
                                                        ? TRANSITION_CAST3_TO_IDLE_TICKS
                                                        : TRANSITION_TO_IDLE_TICKS)
                                        .setAnimation(idleAnimation());
                            }
                            LAST_ANIM_BY_CONTROLLER.put(controller, anim);
                            return PlayState.CONTINUE;
                        }));
    }
}
