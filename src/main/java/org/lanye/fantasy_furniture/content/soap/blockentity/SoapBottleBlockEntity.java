package org.lanye.fantasy_furniture.content.soap.blockentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackData;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackUse;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;

/** 沐浴露 / 洗发露 / 乳霜摞的共用 BE 基类（层列表 + 混合摞 NBT）。 */
public abstract class SoapBottleBlockEntity extends BlockEntity
        implements GeoBlockEntity, SoapBottleStackUse.Holder {

    private final SoapBottleStackData stack;

    protected SoapBottleBlockEntity(
            BlockEntityType<?> type, BlockPos pos, BlockState state, SoapBottleKind hostKind) {
        super(type, pos, state);
        this.stack = new SoapBottleStackData(hostKind);
    }

    protected SoapBottleStackData stack() {
        return stack;
    }

    @Override
    public SoapBottleStackData stackData() {
        return stack;
    }

    @Override
    public void markStackChanged() {
        setChanged();
    }

    public int layerCount() {
        return stack.layerCount();
    }

    public int materialAtLayer(int indexFromBottom) {
        return stack.layerAt(indexFromBottom).materialId();
    }

    public SoapBottleKind kindAtLayer(int indexFromBottom) {
        return stack.layerAt(indexFromBottom).kind();
    }

    public int topMaterial() {
        return stack.topMaterial();
    }

    public List<SoapBottleLayer> layersView() {
        return stack.layersView();
    }

    public void setSingleLayer(SoapBottleKind kind, int materialId) {
        stack.setSingleLayer(kind, materialId);
        setChanged();
    }

    public boolean replaceTopMaterial(int materialId) {
        SoapBottleLayer top = stack.topLayer();
        if (top == null) {
            return false;
        }
        stack.popTopLayer();
        stack.pushLayer(new SoapBottleLayer(top.kind(), materialId));
        setChanged();
        return true;
    }

    protected void loadStack(CompoundTag tag, int legacyMaxStack) {
        stack.load(tag, legacyMaxStack);
    }

    protected void saveStack(CompoundTag tag) {
        stack.save(tag);
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

    protected abstract AnimatableInstanceCache animatableCache();
}
