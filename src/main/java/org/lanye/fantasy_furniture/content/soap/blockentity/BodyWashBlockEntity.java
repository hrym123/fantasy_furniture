package org.lanye.fantasy_furniture.content.soap.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackUse;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 沐浴露摞：纯沐浴露最多 {@link BodyWashAssets#MAX_STACK} 瓶；可与洗发露 / 乳霜混合（混合时最多 4 瓶）。 */
public final class BodyWashBlockEntity extends SoapBottleBlockEntity {

    public static final String MAIN_CONTROLLER = "main";

    private static final String TRIGGER_USE = "use";
    private static final String TRIGGER_USE2 = "use2";
    private static final String TRIGGER_USE3 = "use3";
    private static final String TRIGGER_USE4 = "use4";

    private static final RawAnimation USE_SINGLE =
            RawAnimation.begin().then("animation.body_wash.use", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK2 =
            RawAnimation.begin().then("animation.body_wash_stack.use2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK3 =
            RawAnimation.begin().then("animation.body_wash_stack.use3", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK4 =
            RawAnimation.begin().then("animation.body_wash_stack.use4", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BodyWashBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BODY_WASH.blockEntityType().get(), pos, state, SoapBottleKind.BODY_WASH);
    }

    public void setSingleLayer(int materialId) {
        setSingleLayer(SoapBottleKind.BODY_WASH, materialId);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveStack(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadStack(tag, BodyWashAssets.MAX_STACK);
    }

    public void onServerUseAnim() {
        if (!SoapBottleStackUse.topLayerIs(stack(), SoapBottleKind.BODY_WASH)) {
            return;
        }
        int layers = Math.max(1, layerCount());
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
