package org.lanye.fantasy_furniture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.block.ornament.OrnamentBaseVariant;
import org.lanye.fantasy_furniture.block.ornament.OrnamentFigurineVariant;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * 组合摆件：底座与玩偶各为独立 Geo，变体由 {@link #baseVariant} / {@link #figurineVariant} 驱动。
 */
public class CombinedOrnamentBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String NBT_BASE = "OrnBase";
    private static final String NBT_FIGURINE = "OrnFig";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private OrnamentBaseVariant baseVariant = OrnamentBaseVariant.A;
    private OrnamentFigurineVariant figurineVariant = OrnamentFigurineVariant.A;

    public CombinedOrnamentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.COMBINED_ORNAMENT.blockEntityType().get(), pos, state);
    }

    public OrnamentBaseVariant getBaseVariant() {
        return baseVariant;
    }

    public OrnamentFigurineVariant getFigurineVariant() {
        return figurineVariant;
    }

    /** 服务端：循环切换底座变体并同步客户端。 */
    public void cycleBase() {
        this.baseVariant = this.baseVariant.next();
        markDirtyAndSync();
    }

    /** 服务端：循环切换玩偶变体并同步客户端。 */
    public void cycleFigurine() {
        this.figurineVariant = this.figurineVariant.next();
        markDirtyAndSync();
    }

    private void markDirtyAndSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void readVariants(CompoundTag tag) {
        if (tag.contains(NBT_BASE, Tag.TAG_BYTE)) {
            this.baseVariant = OrnamentBaseVariant.fromOrdinal(tag.getByte(NBT_BASE));
        }
        if (tag.contains(NBT_FIGURINE, Tag.TAG_BYTE)) {
            this.figurineVariant = OrnamentFigurineVariant.fromOrdinal(tag.getByte(NBT_FIGURINE));
        }
    }

    private void writeVariants(CompoundTag tag) {
        tag.putByte(NBT_BASE, (byte) this.baseVariant.ordinal());
        tag.putByte(NBT_FIGURINE, (byte) this.figurineVariant.ordinal());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeVariants(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readVariants(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeVariants(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        readVariants(tag);
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
