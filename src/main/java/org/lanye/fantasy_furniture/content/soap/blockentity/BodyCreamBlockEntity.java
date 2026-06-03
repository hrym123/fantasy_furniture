package org.lanye.fantasy_furniture.content.soap.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 乳霜摞：纯乳霜最多 {@link BodyCreamAssets#MAX_STACK} 瓶；可与沐浴露 / 洗发露混合（混合时最多 4 瓶）。 */
public final class BodyCreamBlockEntity extends SoapBottleBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BodyCreamBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BODY_CREAM.blockEntityType().get(), pos, state, SoapBottleKind.BODY_CREAM);
    }

    public void setSingleLayer(int materialId) {
        setSingleLayer(SoapBottleKind.BODY_CREAM, materialId);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveStack(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadStack(tag, BodyCreamAssets.MAX_STACK);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> PlayState.STOP));
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
