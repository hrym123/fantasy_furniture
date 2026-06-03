package org.lanye.fantasy_furniture.content.soap.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetBottleKind;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetStoredBottle;
import org.lanye.fantasy_furniture.content.soap.block.DisplayCabinetBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 陈列柜：开闭态见方块 {@link DisplayCabinetBlock#OPEN}；仅收纳沐浴露 / 洗发露，LIFO 存入，最多 {@link DisplayCabinetAssets#MAX_BOTTLES} 瓶。 */
public final class DisplayCabinetBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_BOTTLES = "Bottles";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<DisplayCabinetStoredBottle> bottles = new ArrayList<>();

    public DisplayCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DISPLAY_CABINET.blockEntityType().get(), pos, state);
    }

    public boolean isOpen() {
        return getBlockState().getValue(DisplayCabinetBlock.OPEN);
    }

    public int bottleCount() {
        return bottles.size();
    }

    public int washCount() {
        int count = 0;
        for (DisplayCabinetStoredBottle bottle : bottles) {
            if (bottle.kind() == DisplayCabinetBottleKind.BODY_WASH) {
                count++;
            }
        }
        return count;
    }

    public int shampooCount() {
        int count = 0;
        for (DisplayCabinetStoredBottle bottle : bottles) {
            if (bottle.kind() == DisplayCabinetBottleKind.SHAMPOO) {
                count++;
            }
        }
        return count;
    }

    public List<DisplayCabinetStoredBottle> bottlesView() {
        return Collections.unmodifiableList(bottles);
    }

    @Nullable
    public DisplayCabinetStoredBottle bottleAt(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= bottles.size()) {
            return null;
        }
        return bottles.get(indexFromBottom);
    }

    public boolean pushBottle(DisplayCabinetStoredBottle bottle) {
        if (bottles.size() >= DisplayCabinetAssets.MAX_BOTTLES) {
            return false;
        }
        if (bottle.kind() != DisplayCabinetBottleKind.BODY_WASH
                && bottle.kind() != DisplayCabinetBottleKind.SHAMPOO) {
            return false;
        }
        bottles.add(bottle);
        setChanged();
        return true;
    }

    /** 取出最后放入的一瓶（LIFO）；空柜时 {@code null}。 */
    @Nullable
    public DisplayCabinetStoredBottle popBottle() {
        if (bottles.isEmpty()) {
            return null;
        }
        DisplayCabinetStoredBottle removed = bottles.remove(bottles.size() - 1);
        setChanged();
        return removed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (DisplayCabinetStoredBottle bottle : bottles) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Kind", bottle.kind().name());
            entry.putInt("Mat", bottle.materialId());
            list.add(entry);
        }
        tag.put(TAG_BOTTLES, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bottles.clear();
        if (!tag.contains(TAG_BOTTLES, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = tag.getList(TAG_BOTTLES, Tag.TAG_COMPOUND);
        for (Tag entryTag : list) {
            CompoundTag entry = (CompoundTag) entryTag;
            try {
                DisplayCabinetBottleKind kind =
                        DisplayCabinetBottleKind.valueOf(entry.getString("Kind"));
                bottles.add(new DisplayCabinetStoredBottle(kind, entry.getInt("Mat")));
            } catch (IllegalArgumentException ignored) {
                // skip corrupt entry
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
        return cache;
    }
}
