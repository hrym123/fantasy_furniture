package org.lanye.fantasy_furniture.content.furniture.decor.blockentity;

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

/** 抽奖机：右键触发 {@code animation.lottery_machine.draw} 播放一次。 */
public class LotteryMachineBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";
    private static final String TRIGGER_DRAW = "draw";

    private static final String DRAW_ANIM_NAME = "animation.lottery_machine.draw";

    private static final RawAnimation DRAW_ONCE =
            RawAnimation.begin().then(DRAW_ANIM_NAME, Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LotteryMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.LOTTERY_MACHINE.blockEntityType().get(), pos, state);
    }

    /** 服务端：一次交互触发一次抽奖动画。 */
    public void onServerDraw() {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        triggerAnim(MAIN_CONTROLLER, TRIGGER_DRAW);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                        .triggerableAnim(TRIGGER_DRAW, DRAW_ONCE));
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
