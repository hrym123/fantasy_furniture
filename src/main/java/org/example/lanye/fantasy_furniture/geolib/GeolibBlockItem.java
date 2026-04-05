package org.example.lanye.fantasy_furniture.geolib;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.example.lanye.fantasy_furniture.client.model.GeolibBlockItemModel;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 通用 GeckoLib {@link BlockItem}：通过构造传入 {@link GeolibItemAssets}，无需为每个方块物品再写
 * {@code extends BlockItem implements GeoItem}。
 */
public class GeolibBlockItem extends BlockItem implements GeoItem {

    private static final class ClientRendererHolder {
        static final BlockEntityWithoutLevelRenderer INSTANCE = new GeoItemRenderer<>(new GeolibBlockItemModel());
    }

    private final GeolibItemAssets assets;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeolibBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties);
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
                        (AnimationState<GeolibBlockItem> state) -> PlayState.STOP));
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
