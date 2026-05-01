package org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/** 果酱锅：仅在放置时由 {@link org.lanye.fantasy_furniture.content.furniture.kitchen.block.JamPotBlock#setPlacedBy} 触发 {@code animation.jam_pot.enter}。 */
public class JamPotBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";
    private static final String TRIGGER_ENTER = "enter";

    private static final String ENTER_ANIM_NAME = "animation.jam_pot.enter";

    private static final RawAnimation ENTER_ONCE =
            RawAnimation.begin().then(ENTER_ANIM_NAME, Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JamPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.JAM_POT.blockEntityType().get(), pos, state);
    }

    /** 服务端：放置时触发一次入场动画。 */
    public void onServerPlayEnter() {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        triggerAnim(MAIN_CONTROLLER, TRIGGER_ENTER);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                        .triggerableAnim(TRIGGER_ENTER, ENTER_ONCE));
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
