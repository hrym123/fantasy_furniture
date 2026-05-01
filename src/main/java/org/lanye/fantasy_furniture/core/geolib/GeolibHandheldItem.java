package org.lanye.fantasy_furniture.core.geolib;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.furniture.common.client.model.GeolibHandheldItemModel;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 无对应方块的 GeckoLib 手持物品；资源路径见 {@link GeolibItemAssets#itemAsset(String, String)}。
 *
 * @param idleAnimation 默认循环动画全名（如 {@code animation.my_item.idle}），须与 {@code .animation.json} 中键一致。
 */
public class GeolibHandheldItem extends Item implements GeoItem {

    private static final class ClientRendererHolder {
        static final BlockEntityWithoutLevelRenderer INSTANCE = new GeoItemRenderer<>(new GeolibHandheldItemModel());
    }

    private final GeolibItemAssets assets;
    private final RawAnimation idleLoop;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeolibHandheldItem(Properties properties, GeolibItemAssets assets, String idleAnimation) {
        super(properties);
        this.assets = assets;
        this.idleLoop = RawAnimation.begin().then(idleAnimation, Animation.LoopType.LOOP);
        GeoItem.registerSyncedAnimatable(this);
    }

    public GeolibItemAssets assets() {
        return assets;
    }

    protected RawAnimation idleAnimation() {
        return idleLoop;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "idle",
                        0,
                        (AnimationState<GeolibHandheldItem> state) -> {
                            state.getController().setAnimation(state.getAnimatable().idleAnimation());
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
                        return ClientRendererHolder.INSTANCE;
                    }
                });
    }
}
