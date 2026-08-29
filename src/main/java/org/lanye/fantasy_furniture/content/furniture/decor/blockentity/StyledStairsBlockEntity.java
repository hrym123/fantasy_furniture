package org.lanye.fantasy_furniture.content.furniture.decor.blockentity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.StyledStairsRegistration;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledStairsMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.StyledStairsMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.StyledStairsBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 楼梯：共用 geo，贴图随材质档。 */
public class StyledStairsBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StyledStairsBlockEntity(BlockPos pos, BlockState state) {
        super(StyledStairsRegistration.entry().blockEntityType().get(), pos, state);
    }

    public ResourceLocation getTextureLocation() {
        Block block = getBlockState().getBlock();
        if (block instanceof StyledStairsBlock stairs) {
            return StyledStairsMaterials.texture(stairs.variant());
        }
        return StyledStairsMaterials.texture(StyledStairsMaterialVariant.WHITE);
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
