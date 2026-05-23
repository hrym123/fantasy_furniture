package org.lanye.fantasy_furniture.content.soap.blockentity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 肥皂盒方块实体：盒色由方块状态驱动；盒内皂磨损与颜料存 NBT。 */
public class SoapBoxBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_SOAP_WEAR = "SoapWear";
    private static final String TAG_SOAP_MAT = "SoapMat";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private SoapBarAppearance containedSoap = SoapBarAppearance.defaults();

    public SoapBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOAP_BOX.blockEntityType().get(), pos, state);
    }

    public SoapBarAppearance containedSoap() {
        return containedSoap;
    }

    public void setContainedSoap(SoapBarAppearance appearance) {
        this.containedSoap = appearance;
        setChanged();
    }

    public void clearContainedSoap() {
        this.containedSoap = SoapBarAppearance.defaults();
        setChanged();
    }

    public boolean hasContainedSoap(BlockState state) {
        return state.getBlock() instanceof SoapBoxBlock && state.getValue(SoapBoxBlock.HAS_SOAP);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_SOAP_WEAR, containedSoap.wear());
        tag.putInt(TAG_SOAP_MAT, containedSoap.materialId());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int wear = tag.contains(TAG_SOAP_WEAR) ? tag.getInt(TAG_SOAP_WEAR) : SoapBarAppearance.DEFAULT_WEAR;
        int mat = tag.contains(TAG_SOAP_MAT) ? tag.getInt(TAG_SOAP_MAT) : SoapBarAppearance.DEFAULT_MATERIAL;
        containedSoap = new SoapBarAppearance(wear, mat);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
