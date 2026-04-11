package org.lanye.fantasy_furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * 搅拌碗动画：每次原版对方块交互（{@link org.lanye.fantasy_furniture.block.facing.MixingBowlBlock#use}）触发一次播放。
 */
public class MixingBowlBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";
    private static final String TRIGGER_SHORT = "short";

    private static final String STIR_ANIM_NAME = "animation.mixing_bowl.stir";

    private static final RawAnimation STIR_ONCE =
            RawAnimation.begin().then(STIR_ANIM_NAME, Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MixingBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MIXING_BOWL.blockEntityType().get(), pos, state);
    }

    /** 服务端：一次交互触发一次可同步的短动画。 */
    public void onServerShortStir() {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        triggerAnim(MAIN_CONTROLLER, TRIGGER_SHORT);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                        .triggerableAnim(TRIGGER_SHORT, STIR_ONCE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
