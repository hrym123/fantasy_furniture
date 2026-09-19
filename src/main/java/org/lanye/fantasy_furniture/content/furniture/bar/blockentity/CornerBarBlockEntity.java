package org.lanye.fantasy_furniture.content.furniture.bar.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.bootstrap.block.CornerBarRegistration;
import org.lanye.fantasy_furniture.content.furniture.bar.BarMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.bar.BarMaterials;
import org.lanye.fantasy_furniture.content.furniture.bar.block.CornerBarBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/** 转角吧台：GeckoLib 静态模型（含 {@code animation.corner_bar.idle} 占位）。 */
public class CornerBarBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CornerBarBlockEntity(BlockPos pos, BlockState state) {
        super(CornerBarRegistration.entry().blockEntityType().get(), pos, state);
    }

    public ResourceLocation getTextureLocation() {
        Block block = getBlockState().getBlock();
        if (block instanceof CornerBarBlock bar) {
            return BarMaterials.sharedTexture(bar.variant());
        }
        return BarMaterials.sharedTexture(BarMaterialVariant.PURPLE_BAR_2);
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
