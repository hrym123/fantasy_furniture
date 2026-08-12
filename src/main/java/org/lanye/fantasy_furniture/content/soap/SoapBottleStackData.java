package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** 瓶罐摞 BE 侧层列表：种类 + 颜料；可选架/盒载体（特殊 2/3）；兼容旧版仅 {@code Mat} 的存档。 */
public final class SoapBottleStackData {

    private static final String TAG_LAYERS = "BottleLayers";
    private static final String TAG_LAYER_MATS = "LayerMats";
    private static final String TAG_LAYERS_LEGACY = "Layers";
    private static final String TAG_CARRIER_KIND = "CarrierKind";
    private static final String TAG_CARRIER_INTERMEDIATE = "CarrierIntermediate";
    private static final String TAG_CARRIER_BOX_MAT = "CarrierBoxMat";

    private final SoapBottleKind hostKind;
    private final List<SoapBottleLayer> layers = new ArrayList<>();
    @Nullable private SoapStackCarrierKind carrier;
    private boolean carrierIntermediate;
    private int carrierBoxMaterialId = SoapBoxAppearance.DEFAULT_MATERIAL;

    public SoapBottleStackData(SoapBottleKind hostKind) {
        this.hostKind = hostKind;
    }

    public SoapBottleKind hostKind() {
        return hostKind;
    }

    public int layerCount() {
        return layers.size();
    }

    public List<SoapBottleLayer> layersView() {
        return Collections.unmodifiableList(layers);
    }

    @Nullable
    public SoapStackCarrierKind carrier() {
        return carrier;
    }

    public boolean carrierIntermediate() {
        return carrierIntermediate;
    }

    public int carrierBoxMaterialId() {
        return carrierBoxMaterialId;
    }

    public boolean hasCarrier() {
        return carrier != null;
    }

    public SoapBottleLayer layerAt(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= layers.size()) {
            return new SoapBottleLayer(hostKind, hostKind.defaultMaterial());
        }
        return layers.get(indexFromBottom);
    }

    @Nullable
    public SoapBottleLayer topLayer() {
        if (layers.isEmpty()) {
            return null;
        }
        return layers.get(layers.size() - 1);
    }

    public int topMaterial() {
        SoapBottleLayer top = topLayer();
        return top != null ? top.materialId() : hostKind.defaultMaterial();
    }

    /** 原地替换顶层颜料（不走 {@link #pushLayer} 接受规则，避免打断载体态）。 */
    public boolean replaceTopMaterial(int materialId) {
        if (layers.isEmpty()) {
            return false;
        }
        SoapBottleLayer top = layers.get(layers.size() - 1);
        if (!top.kind().isValidMaterial(materialId)) {
            return false;
        }
        layers.set(layers.size() - 1, new SoapBottleLayer(top.kind(), materialId));
        return true;
    }

    public void setSingleLayer(SoapBottleKind kind, int materialId) {
        layers.clear();
        clearCarrierFields();
        layers.add(new SoapBottleLayer(kind, materialId));
    }

    public boolean pushLayer(SoapBottleLayer layer) {
        if (!SoapBottleStackRules.canAcceptBottle(this, layer.kind())) {
            return false;
        }
        if (carrier != null && carrierIntermediate && layer.kind() == SoapBottleKind.BODY_CREAM) {
            return tryPushCreamAfterCarrier(layer.materialId());
        }
        layers.add(layer);
        return true;
    }

    public boolean tryPushCarrier(SoapStackCarrierKind kind) {
        return tryPushCarrier(kind, SoapBoxAppearance.DEFAULT_MATERIAL);
    }

    public boolean tryPushCarrier(SoapStackCarrierKind kind, int boxMaterialId) {
        if (!SoapBottleStackRules.canAcceptCarrier(this, kind)) {
            return false;
        }
        // 特殊 3：恰好 2 瓶 → 中间态；特殊 2：3 瓶顶乳霜 → 完成态
        boolean intermediate = layers.size() == 2;
        this.carrier = kind;
        this.carrierIntermediate = intermediate;
        this.carrierBoxMaterialId =
                kind == SoapStackCarrierKind.BOX
                        ? boxMaterialId
                        : SoapBoxAppearance.DEFAULT_MATERIAL;
        return true;
    }

    /**
     * 特殊 3：中间态已有架/盒时再放乳霜 → 乳霜成为第 3 位，载体转为完成态第 4 位。
     */
    public boolean tryPushCreamAfterCarrier(int materialId) {
        if (carrier == null || !carrierIntermediate) {
            return false;
        }
        if (layers.size() != 2) {
            return false;
        }
        layers.add(new SoapBottleLayer(SoapBottleKind.BODY_CREAM, materialId));
        carrierIntermediate = false;
        return true;
    }

    /** 有载体时优先弹出载体物品；否则 null。 */
    @Nullable
    public ItemStack popCarrierItem() {
        if (carrier == null) {
            return null;
        }
        ItemStack drop = carrier.toItemStack(carrierBoxMaterialId);
        clearCarrierFields();
        return drop;
    }

    @Nullable
    public SoapBottleLayer popTopLayer() {
        if (layers.isEmpty()) {
            return null;
        }
        return layers.remove(layers.size() - 1);
    }

    public void clear() {
        layers.clear();
        clearCarrierFields();
    }

    private void clearCarrierFields() {
        carrier = null;
        carrierIntermediate = false;
        carrierBoxMaterialId = SoapBoxAppearance.DEFAULT_MATERIAL;
    }

    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (SoapBottleLayer layer : layers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Kind", layer.kind().name());
            entry.putInt("Mat", layer.materialId());
            list.add(entry);
        }
        tag.put(TAG_LAYERS, list);
        if (carrier != null) {
            tag.putString(TAG_CARRIER_KIND, carrier.name());
            tag.putBoolean(TAG_CARRIER_INTERMEDIATE, carrierIntermediate);
            if (carrier == SoapStackCarrierKind.BOX) {
                tag.putInt(TAG_CARRIER_BOX_MAT, carrierBoxMaterialId);
            }
        } else {
            tag.remove(TAG_CARRIER_KIND);
            tag.remove(TAG_CARRIER_INTERMEDIATE);
            tag.remove(TAG_CARRIER_BOX_MAT);
        }
    }

    public void load(CompoundTag tag, int legacyMaxStack) {
        layers.clear();
        clearCarrierFields();
        if (tag.contains(TAG_LAYERS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LAYERS, Tag.TAG_COMPOUND);
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                SoapBottleKind kind = parseKind(entry.getString("Kind"));
                int mat = entry.getInt("Mat");
                if (kind.isValidMaterial(mat)) {
                    layers.add(new SoapBottleLayer(kind, mat));
                }
            }
            loadCarrier(tag);
            if (!layers.isEmpty()) {
                return;
            }
        }
        if (tag.contains(TAG_LAYER_MATS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LAYER_MATS, Tag.TAG_COMPOUND);
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                int mat = entry.getInt("Mat");
                if (hostKind.isValidMaterial(mat)) {
                    layers.add(new SoapBottleLayer(hostKind, mat));
                }
            }
            loadCarrier(tag);
            if (!layers.isEmpty()) {
                return;
            }
        }
        int legacy = tag.contains(TAG_LAYERS_LEGACY) ? tag.getInt(TAG_LAYERS_LEGACY) : 1;
        int count = Math.max(1, Math.min(legacyMaxStack, legacy));
        for (int i = 0; i < count; i++) {
            layers.add(new SoapBottleLayer(hostKind, hostKind.defaultMaterial()));
        }
        loadCarrier(tag);
    }

    private void loadCarrier(CompoundTag tag) {
        if (!tag.contains(TAG_CARRIER_KIND, Tag.TAG_STRING)) {
            return;
        }
        try {
            carrier = SoapStackCarrierKind.valueOf(tag.getString(TAG_CARRIER_KIND));
        } catch (IllegalArgumentException ex) {
            clearCarrierFields();
            return;
        }
        carrierIntermediate = tag.getBoolean(TAG_CARRIER_INTERMEDIATE);
        if (carrier == SoapStackCarrierKind.BOX && tag.contains(TAG_CARRIER_BOX_MAT)) {
            int mat = tag.getInt(TAG_CARRIER_BOX_MAT);
            carrierBoxMaterialId =
                    SoapBarMaterials.isValid(mat) ? mat : SoapBoxAppearance.DEFAULT_MATERIAL;
        }
    }

    private SoapBottleKind parseKind(String name) {
        try {
            return SoapBottleKind.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return hostKind;
        }
    }
}
