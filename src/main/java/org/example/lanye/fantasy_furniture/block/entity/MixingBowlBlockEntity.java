package org.example.lanye.fantasy_furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.example.lanye.fantasy_furniture.block.ModBlocks;
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
 * 搅拌碗动画：短按为可触发的一次播放；长按（由客户端发包）为循环，松手或准心离开即停。
 */
public class MixingBowlBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";
    private static final String TRIGGER_SHORT = "short";

    private static final String STIR_ANIM_NAME = "animation.mixing_bowl.stir";

    private static final RawAnimation STIR_LOOP = RawAnimation.begin().thenLoop(STIR_ANIM_NAME);

    private static final RawAnimation STIR_ONCE =
            RawAnimation.begin().then(STIR_ANIM_NAME, Animation.LoopType.PLAY_ONCE);

    /** 服务端与同步：长按循环是否开启 */
    private boolean holdLoop;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MixingBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MIXING_BOWL.blockEntityType().get(), pos, state);
    }

    public boolean isHoldLoop() {
        return holdLoop;
    }

    /** 短按结束：服务端触发一次播放（同步到其他客户端）。 */
    public void onServerShortStir() {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        if (holdLoop) {
            return;
        }
        triggerAnim(MAIN_CONTROLLER, TRIGGER_SHORT);
    }

    /** 长按循环开始/结束。 */
    public void onServerHoldStir(boolean active) {
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        this.holdLoop = active;
        setChanged();
        syncToClients();
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            BlockState st = getBlockState();
            level.sendBlockUpdated(worldPosition, st, st, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> {
                            if (holdLoop) {
                                return state.setAndContinue(STIR_LOOP);
                            }
                            return PlayState.STOP;
                        })
                        .triggerableAnim(TRIGGER_SHORT, STIR_ONCE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("HoldLoop", holdLoop);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        holdLoop = tag.getBoolean("HoldLoop");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("HoldLoop", holdLoop);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        holdLoop = tag.getBoolean("HoldLoop");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
