package org.lanye.fantasy_furniture.content.soap.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackUse;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 洗发露摞：纯洗发露最多 {@link ShampooAssets#MAX_STACK} 瓶；可与沐浴露 / 乳霜混合（混合时最多 4 瓶）。 */
public final class ShampooBlockEntity extends SoapBottleBlockEntity {

    public static final String MAIN_CONTROLLER = "main";

    private static final String TRIGGER_USE = "use";
    private static final String TRIGGER_USE2 = "use2";
    private static final String TRIGGER_USE3 = "use3";
    private static final String TRIGGER_USE4 = "use4";

    private static final RawAnimation USE_SINGLE =
            RawAnimation.begin().then("animation.shampoo.use", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK2 =
            RawAnimation.begin().then("animation.shampoo_stack.use2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK3 =
            RawAnimation.begin().then("animation.shampoo_stack.use3", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK4 =
            RawAnimation.begin().then("animation.shampoo_stack.use4", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShampooBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SHAMPOO.blockEntityType().get(), pos, state, SoapBottleKind.SHAMPOO);
    }

    public int visibleLayerCount() {
        if (layerCount() > 0) {
            return layerCount();
        }
        BlockState state = getBlockState();
        if (state.hasProperty(ShampooBlock.LAYERS)) {
            return Math.max(1, state.getValue(ShampooBlock.LAYERS));
        }
        return 1;
    }

    public void setSingleLayer(int materialId) {
        setSingleLayer(SoapBottleKind.SHAMPOO, materialId);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveStack(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadStack(tag, ShampooAssets.MAX_STACK);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        reconcileLayersFromBlockState();
    }

    private void reconcileLayersFromBlockState() {
        if (layerCount() > 0) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(ShampooBlock.LAYERS) || !state.hasProperty(ShampooBlock.MATERIAL)) {
            return;
        }
        int count = Math.max(1, state.getValue(ShampooBlock.LAYERS));
        int mat = state.getValue(ShampooBlock.MATERIAL);
        for (int i = 0; i < count; i++) {
            stack().pushLayer(new SoapBottleLayer(SoapBottleKind.SHAMPOO, mat));
        }
    }

    public void triggerUseAnim() {
        if (!SoapBottleStackUse.topLayerIs(stack(), SoapBottleKind.SHAMPOO)) {
            return;
        }
        int layers = visibleLayerCount();
        String trigger =
                switch (layers) {
                    case 1 -> TRIGGER_USE;
                    case 2 -> TRIGGER_USE2;
                    case 3 -> TRIGGER_USE3;
                    default -> TRIGGER_USE4;
                };
        triggerAnim(MAIN_CONTROLLER, trigger);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                        .triggerableAnim(TRIGGER_USE, USE_SINGLE)
                        .triggerableAnim(TRIGGER_USE2, USE_STACK2)
                        .triggerableAnim(TRIGGER_USE3, USE_STACK3)
                        .triggerableAnim(TRIGGER_USE4, USE_STACK4));
    }

    @Override
    protected AnimatableInstanceCache animatableCache() {
        return cache;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
