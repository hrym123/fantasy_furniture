package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BedPlate6AccessoryGeoItemRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 床板 6 床单 / 被套 / 枕头等「仅数据物品」：创造栏与手持使用与方块实体相同的 {@code geo/block/*.geo.json}，
 * 而非 {@code models/block} 里的简化立方体。
 */
public abstract class BedPlate6GeolibDecorItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected BedPlate6GeolibDecorItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "idle",
                        0,
                        (AnimationState<BedPlate6GeolibDecorItem> state) -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new BedPlate6AccessoryGeoItemRenderer();
                        }
                        return renderer;
                    }
                });
    }
}
