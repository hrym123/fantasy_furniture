package org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetYaw;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 柜子：三层各存一件 ItemStack + 展品竖直轴偏航；世界内以 BER FIXED 展示。 */
public final class CabinetBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SLOT = "Slot";
    private static final String TAG_ITEM_ROT = "Rot";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final NonNullList<ItemStack> items =
            NonNullList.withSize(CabinetSlot.COUNT, ItemStack.EMPTY);
    /** 各层展品绕竖直轴偏航 0～7（步长 45°）。 */
    private final int[] itemYaw = new int[CabinetSlot.COUNT];

    public CabinetBlockEntity(BlockPos pos, BlockState state) {
        super(typeFor(state), pos, state);
    }

    private static BlockEntityType<?> typeFor(BlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.CABINET_1.block().get()) {
            return ModBlocks.CABINET_1.blockEntityType().get();
        }
        if (block == ModBlocks.CABINET_2.block().get()) {
            return ModBlocks.CABINET_2.blockEntityType().get();
        }
        throw new IllegalStateException("CabinetBlockEntity on unexpected block: " + block);
    }

    public CabinetKind kind() {
        Block block = getBlockState().getBlock();
        if (block instanceof CabinetBlock cabinet) {
            return cabinet.kind();
        }
        return CabinetKind.CABINET_1;
    }

    public ItemStack getItem(CabinetSlot slot) {
        return items.get(slot.index());
    }

    public boolean isEmpty(CabinetSlot slot) {
        return getItem(slot).isEmpty();
    }

    /** 展品竖直轴偏航步（0～7）。 */
    public int itemYaw(CabinetSlot slot) {
        return CabinetYaw.clamp(itemYaw[slot.index()]);
    }

    /** 有物槽：展品 +45°；成功返回 true。 */
    public boolean rotateItem(CabinetSlot slot) {
        if (isEmpty(slot)) {
            return false;
        }
        itemYaw[slot.index()] = CabinetYaw.next(itemYaw[slot.index()]);
        setChanged();
        sync();
        return true;
    }

    /** 向空槽放入（消耗调用方负责）；成功返回 true。 */
    public boolean placeItem(CabinetSlot slot, ItemStack stack) {
        if (stack.isEmpty() || !isEmpty(slot)) {
            return false;
        }
        items.set(slot.index(), stack.copyWithCount(1));
        itemYaw[slot.index()] = 0;
        setChanged();
        sync();
        return true;
    }

    /** 取出槽内整件；空槽返回 EMPTY。 */
    public ItemStack takeItem(CabinetSlot slot) {
        ItemStack stack = items.get(slot.index());
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.set(slot.index(), ItemStack.EMPTY);
        itemYaw[slot.index()] = 0;
        setChanged();
        sync();
        return stack;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        Containers.dropContents(level, worldPosition, items);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
            itemYaw[i] = 0;
        }
        setChanged();
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (CabinetSlot slot : CabinetSlot.values()) {
            ItemStack stack = items.get(slot.index());
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putByte(TAG_SLOT, (byte) slot.index());
            int yaw = itemYaw[slot.index()];
            if (yaw != 0) {
                entry.putByte(TAG_ITEM_ROT, (byte) yaw);
            }
            stack.save(entry);
            list.add(entry);
        }
        tag.put(TAG_ITEMS, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
            itemYaw[i] = 0;
        }
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte(TAG_SLOT) & 0xFF;
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of(entry));
                itemYaw[slot] = entry.contains(TAG_ITEM_ROT, Tag.TAG_BYTE)
                        ? CabinetYaw.clamp(entry.getByte(TAG_ITEM_ROT))
                        : 0;
            }
        }
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
        return this.cache;
    }

    /** 三格高柜需扩大渲染包围盒，否则上层展品会被裁掉。 */
    @Override
    public AABB getRenderBoundingBox() {
        double h = kind().heightBlocks();
        return new AABB(
                worldPosition.getX() - 0.5,
                worldPosition.getY(),
                worldPosition.getZ() - 0.5,
                worldPosition.getX() + 1.5,
                worldPosition.getY() + h,
                worldPosition.getZ() + 1.5);
    }
}
