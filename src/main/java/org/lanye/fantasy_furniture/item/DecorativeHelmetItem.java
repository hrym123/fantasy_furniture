package org.lanye.fantasy_furniture.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.client.renderer.DecorativeHelmetGeoItemRenderer;
import org.lanye.fantasy_furniture.geolib.GeolibItemAssets;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 纯装饰头饰：右键与头部装备槽按原版 {@link Equipable#swapWithEquipmentSlot} 互换（含绑定诅咒、统计等）；
 * 头戴由 {@link org.lanye.fantasy_furniture.client.DecorativeHelmetPlayerLayer} + GeckoLib BER 绘制。
 */
public class DecorativeHelmetItem extends Item implements GeoItem, Equipable {

    private final GeolibItemAssets assets;
    private final RawAnimation idleAnimation;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * @param idleAnimationName Bedrock 动画名，须与 {@code animations/item/&lt;basename&gt;.animation.json} 内键一致，例如
     *     {@code animation.decorative_helmet_pink_top_hat.idle}。
     */
    public DecorativeHelmetItem(Properties properties, GeolibItemAssets assets, String idleAnimationName) {
        super(properties);
        this.assets = assets;
        this.idleAnimation = RawAnimation.begin().then(idleAnimationName, Animation.LoopType.LOOP);
        GeoItem.registerSyncedAnimatable(this);
    }

    public GeolibItemAssets assets() {
        return assets;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "idle",
                        0,
                        (AnimationState<DecorativeHelmetItem> state) -> {
                            state.getController().setAnimation(this.idleAnimation);
                            return PlayState.CONTINUE;
                        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return DecorativeHelmetGeoItemRenderer.INSTANCE;
                    }
                });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}
