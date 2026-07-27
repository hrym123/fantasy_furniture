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
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPackagingStackOps;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAssets;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxMaterials;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 包装盒摞：最多 {@link SoapPaperBoxAssets#MAX_STACK} 层，LIFO；可选每层带皂。 */
public final class SoapPaperBoxBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_LAYER_MATS = "LayerMats";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<Integer> layerMaterials = new ArrayList<>();
    private final List<SoapBarAppearance> layerSoaps = new ArrayList<>();

    public SoapPaperBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOAP_PAPER_BOX.blockEntityType().get(), pos, state);
    }

    public int layerCount() {
        return layerMaterials.size();
    }

    public int materialAtLayer(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= layerMaterials.size()) {
            return SoapPaperBoxMaterials.DEFAULT;
        }
        return layerMaterials.get(indexFromBottom);
    }

    public int topMaterial() {
        if (layerMaterials.isEmpty()) {
            return SoapPaperBoxMaterials.DEFAULT;
        }
        return layerMaterials.get(layerMaterials.size() - 1);
    }

    public List<Integer> layerMaterialsView() {
        return Collections.unmodifiableList(layerMaterials);
    }

    public boolean isSoapStack() {
        for (SoapBarAppearance soap : layerSoaps) {
            if (soap != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmptyPackagingStack() {
        return !isSoapStack();
    }

    @Nullable
    public SoapBarAppearance packagedSoapAt(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= layerMaterials.size()) {
            return null;
        }
        SoapBarAppearance body = layerSoaps.get(indexFromBottom);
        if (body == null) {
            return null;
        }
        return SoapPackagingStackOps.boxedFromLayer(layerMaterials.get(indexFromBottom), body);
    }

    public void clearLayers() {
        layerMaterials.clear();
        layerSoaps.clear();
        setChanged();
    }

    public void setSingleLayer(int materialId) {
        layerMaterials.clear();
        layerSoaps.clear();
        layerMaterials.add(materialId);
        layerSoaps.add(null);
        setChanged();
    }

    public boolean pushLayer(int materialId) {
        if (isSoapStack() || layerMaterials.size() >= SoapPaperBoxAssets.MAX_STACK) {
            return false;
        }
        layerMaterials.add(materialId);
        layerSoaps.add(null);
        setChanged();
        return true;
    }

    public boolean pushSoapLayer(SoapBarAppearance packaged) {
        if (!packaged.isBoxed() || packaged.packagingTorn()) {
            return false;
        }
        if (isEmptyPackagingStack() && !layerMaterials.isEmpty()) {
            return false;
        }
        if (layerMaterials.size() >= SoapPaperBoxAssets.MAX_STACK) {
            return false;
        }
        layerMaterials.add(packaged.boxMaterialId());
        layerSoaps.add(
                new SoapBarAppearance(
                        packaged.wear(),
                        packaged.materialId(),
                        0,
                        false,
                        packaged.particleMatId(),
                        0));
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
        layerSoaps.remove(layerSoaps.size() - 1);
        int removed = layerMaterials.remove(layerMaterials.size() - 1);
        setChanged();
        return removed;
    }

    @Nullable
    public Object popTop() {
        if (layerMaterials.isEmpty()) {
            return null;
        }
        int index = layerMaterials.size() - 1;
        SoapBarAppearance packaged = packagedSoapAt(index);
        layerSoaps.remove(index);
        int mat = layerMaterials.remove(index);
        setChanged();
        return packaged != null ? packaged : mat;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int i = 0; i < layerMaterials.size(); i++) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Mat", layerMaterials.get(i));
            SoapBarAppearance soap = layerSoaps.get(i);
            if (soap != null) {
                SoapPackagingStackOps.writeSoapBodyToLayerTag(entry, soap);
            }
            list.add(entry);
        }
        tag.put(TAG_LAYER_MATS, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        layerMaterials.clear();
        layerSoaps.clear();
        if (!tag.contains(TAG_LAYER_MATS, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = tag.getList(TAG_LAYER_MATS, Tag.TAG_COMPOUND);
        for (Tag entryTag : list) {
            CompoundTag entry = (CompoundTag) entryTag;
            int mat = entry.getInt("Mat");
            if (SoapPaperBoxMaterials.isValid(mat)) {
                layerMaterials.add(mat);
                layerSoaps.add(SoapPackagingStackOps.readSoapBodyFromLayerTag(entry));
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
