package org.lanye.fantasy_furniture.content.furniture.decor.blockentity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.decor.block.ComputerBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 电脑：静态 Geo；开合由方块状态 {@link ComputerBlock#OPEN} 驱动。 */
public final class ComputerBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(typeFor(state), pos, state);
    }

    private static BlockEntityType<?> typeFor(BlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.COMPUTER_1.block().get()) {
            return ModBlocks.COMPUTER_1.blockEntityType().get();
        }
        if (block == ModBlocks.COMPUTER_2.block().get()) {
            return ModBlocks.COMPUTER_2.blockEntityType().get();
        }
        throw new IllegalStateException("ComputerBlockEntity on unexpected block: " + block);
    }

    public String closedAssetId() {
        Block block = getBlockState().getBlock();
        if (block instanceof ComputerBlock computer) {
            return computer.closedAssetId();
        }
        return "computer_1";
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> PlayState.STOP));
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
