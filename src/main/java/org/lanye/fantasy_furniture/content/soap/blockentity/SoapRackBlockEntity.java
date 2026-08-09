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
import org.lanye.fantasy_furniture.content.soap.SoapBarDurability;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapRackComboRules;
import org.lanye.fantasy_furniture.content.soap.SoapRackComboScheme;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 肥皂架方块实体：架上皂 + 瓶罐组合陈列（SOAP-006）。 */
public class SoapRackBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_SOAP_DURABILITY = "SoapDurability";
    private static final String TAG_SOAP_WEAR_LEGACY = "SoapWear";
    private static final String TAG_SOAP_MAT = "SoapMat";
    private static final String TAG_BOTTLES = "RackBottles";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private SoapBarAppearance containedSoap = SoapBarAppearance.defaults();
    private final List<SoapBottleLayer> bottles = new ArrayList<>();

    public SoapRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOAP_RACK.blockEntityType().get(), pos, state);
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

    public boolean hasSoapState() {
        return getBlockState().getValue(SoapRackBlock.HAS_SOAP);
    }

    public int bottleCount() {
        return bottles.size();
    }

    public List<SoapBottleLayer> bottlesView() {
        return Collections.unmodifiableList(bottles);
    }

    public boolean pushBottle(SoapBottleLayer layer) {
        if (!SoapRackComboRules.canAccept(bottles, layer.kind())) {
            return false;
        }
        bottles.add(layer);
        setChanged();
        return true;
    }

    @Nullable
    public SoapBottleLayer popBottle() {
        if (bottles.isEmpty()) {
            return null;
        }
        SoapBottleLayer removed = bottles.remove(bottles.size() - 1);
        setChanged();
        return removed;
    }

    @Nullable
    public SoapRackComboScheme completedComboScheme() {
        return SoapRackComboRules.completedScheme(bottles, hasSoapState());
    }

    public boolean forbidsSoapBoxOnTop() {
        return SoapRackComboRules.forbidsSoapBoxOnTop(bottles, hasSoapState());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_SOAP_DURABILITY, containedSoap.durability());
        tag.remove(TAG_SOAP_WEAR_LEGACY);
        tag.putInt(TAG_SOAP_MAT, containedSoap.materialId());
        ListTag list = new ListTag();
        for (SoapBottleLayer layer : bottles) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Kind", layer.kind().name());
            entry.putInt("Mat", layer.materialId());
            list.add(entry);
        }
        tag.put(TAG_BOTTLES, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int durability =
                tag.contains(TAG_SOAP_DURABILITY)
                        ? tag.getInt(TAG_SOAP_DURABILITY)
                        : tag.contains(TAG_SOAP_WEAR_LEGACY)
                                ? SoapBarDurability.fromLegacyWear(
                                        tag.getInt(TAG_SOAP_WEAR_LEGACY))
                                : SoapBarAppearance.DEFAULT_DURABILITY;
        int mat = tag.contains(TAG_SOAP_MAT) ? tag.getInt(TAG_SOAP_MAT) : SoapBarAppearance.DEFAULT_MATERIAL;
        containedSoap = new SoapBarAppearance(durability, mat);
        bottles.clear();
        if (tag.contains(TAG_BOTTLES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_BOTTLES, Tag.TAG_COMPOUND);
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                SoapBottleKind kind = parseKind(entry.getString("Kind"));
                if (kind == null) {
                    continue;
                }
                int bottleMat = entry.getInt("Mat");
                if (kind.isValidMaterial(bottleMat)) {
                    bottles.add(new SoapBottleLayer(kind, bottleMat));
                }
            }
        }
    }

    @Nullable
    private static SoapBottleKind parseKind(String name) {
        try {
            return SoapBottleKind.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return null;
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
