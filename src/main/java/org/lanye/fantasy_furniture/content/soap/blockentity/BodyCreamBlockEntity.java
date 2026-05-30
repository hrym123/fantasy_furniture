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
import org.lanye.fantasy_furniture.content.soap.BodyCreamAssets;
import org.lanye.fantasy_furniture.content.soap.BodyCreamMaterials;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 乳霜摞：最多 {@link BodyCreamAssets#MAX_STACK} 瓶，LIFO；每层独立颜料。 */
public final class BodyCreamBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_LAYER_MATS = "LayerMats";
    private static final String TAG_LAYERS_LEGACY = "Layers";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<Integer> layerMaterials = new ArrayList<>();

    public BodyCreamBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BODY_CREAM.blockEntityType().get(), pos, state);
    }

    public int layerCount() {
        return layerMaterials.size();
    }

    public int materialAtLayer(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= layerMaterials.size()) {
            return BodyCreamMaterials.DEFAULT;
        }
        return layerMaterials.get(indexFromBottom);
    }

    public int topMaterial() {
        if (layerMaterials.isEmpty()) {
            return BodyCreamMaterials.DEFAULT;
        }
        return layerMaterials.get(layerMaterials.size() - 1);
    }

    public List<Integer> layerMaterialsView() {
        return Collections.unmodifiableList(layerMaterials);
    }

    public void setSingleLayer(int materialId) {
        layerMaterials.clear();
        layerMaterials.add(materialId);
        setChanged();
    }

    public boolean pushLayer(int materialId) {
        if (layerMaterials.size() >= BodyCreamAssets.MAX_STACK) {
            return false;
        }
        layerMaterials.add(materialId);
        setChanged();
        return true;
    }

    public boolean replaceTopMaterial(int materialId) {
        if (layerMaterials.isEmpty()) {
            return false;
        }
        layerMaterials.set(layerMaterials.size() - 1, materialId);
        setChanged();
        return true;
    }

    @Nullable
    public Integer popTopLayer() {
        if (layerMaterials.isEmpty()) {
            return null;
        }
        int removed = layerMaterials.remove(layerMaterials.size() - 1);
        setChanged();
        return removed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int mat : layerMaterials) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Mat", mat);
            list.add(entry);
        }
        tag.put(TAG_LAYER_MATS, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        layerMaterials.clear();
        if (tag.contains(TAG_LAYER_MATS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LAYER_MATS, Tag.TAG_COMPOUND);
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                int mat = entry.getInt("Mat");
                if (BodyCreamMaterials.isValid(mat)) {
                    layerMaterials.add(mat);
                }
            }
            if (!layerMaterials.isEmpty()) {
                return;
            }
        }
        int legacy = tag.contains(TAG_LAYERS_LEGACY) ? tag.getInt(TAG_LAYERS_LEGACY) : 1;
        int count = Math.max(1, Math.min(BodyCreamAssets.MAX_STACK, legacy));
        for (int i = 0; i < count; i++) {
            layerMaterials.add(BodyCreamMaterials.DEFAULT);
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
