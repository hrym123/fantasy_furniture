package org.lanye.fantasy_furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.block.ModBlocks;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/** 捣蒜碗：交互触发 {@code animation.pestle_bowl.mash} 一次完整播放。 */
public class PestleBowlBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";
    private static final String TRIGGER_MASH = "mash";

    private static final String MASH_ANIM_NAME = "animation.pestle_bowl.mash";

    private static final RawAnimation MASH_ONCE =
            RawAnimation.begin().then(MASH_ANIM_NAME, Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PestleBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PESTLE_BOWL.blockEntityType().get(), pos, state);
    }

    /** 服务端：一次交互触发一次捣碎动画。 */
    public void onServerMash() {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        triggerAnim(MAIN_CONTROLLER, TRIGGER_MASH);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                        .triggerableAnim(TRIGGER_MASH, MASH_ONCE));
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
