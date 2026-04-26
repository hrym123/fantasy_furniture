package org.lanye.fantasy_furniture.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
 * 纯装饰头饰：支持右键快速装备到头盔槽；头戴与第三人称由 GeckoLib geo 渲染，GUI 仍用扁平图标模型。
 */
public class DecorativeHelmetItem extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin()
            .then("animation.decorative_helmet_blue_top_hat.idle", Animation.LoopType.LOOP);

    private final GeolibItemAssets assets;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DecorativeHelmetItem(Properties properties, GeolibItemAssets assets) {
        super(properties);
        this.assets = assets;
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
                            state.getController().setAnimation(IDLE);
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
        ItemStack inHand = player.getItemInHand(hand);
        ItemStack currentHead = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!currentHead.isEmpty()) {
            return InteractionResultHolder.fail(inHand);
        }

        ItemStack equipStack = inHand.copyWithCount(1);
        player.setItemSlot(EquipmentSlot.HEAD, equipStack);
        if (!player.getAbilities().instabuild) {
            inHand.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(inHand, level.isClientSide());
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return armorType == EquipmentSlot.HEAD;
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}
