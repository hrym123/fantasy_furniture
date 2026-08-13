package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;

/**
 * 瓶罐摞 BE：定长槽位（可空）+ 可选架/盒载体。
 *
 * <p>槽 0…3 对应陈列位 1…4；纯乳霜可至槽 4（第 5 位）。取出只清空该槽，不挤位。
 */
public final class SoapBottleStackData {

    public static final int MAX_SLOTS = BodyCreamAssets.MAX_STACK;

    private static final String TAG_LAYERS = "BottleLayers";
    private static final String TAG_LAYER_MATS = "LayerMats";
    private static final String TAG_LAYERS_LEGACY = "Layers";
    private static final String TAG_CARRIER_KIND = "CarrierKind";
    private static final String TAG_CARRIER_INTERMEDIATE = "CarrierIntermediate";
    private static final String TAG_CARRIER_BOX_MAT = "CarrierBoxMat";
    private static final String TAG_CARRIER_HAS_SOAP = "CarrierHasSoap";
    private static final String TAG_CARRIER_SOAP = "CarrierSoap";
    private static final String TAG_CARRIER_BOX_OPEN = "CarrierBoxOpen";

    private final SoapBottleKind hostKind;
    private final SoapBottleLayer[] slots = new SoapBottleLayer[MAX_SLOTS];
    @Nullable private SoapStackCarrierKind carrier;
    private boolean carrierIntermediate;
    private int carrierBoxMaterialId = SoapBoxAppearance.DEFAULT_MATERIAL;
    private boolean carrierHasSoap;
    @Nullable private SoapBarAppearance carrierSoap;
    private boolean carrierBoxOpen;

    public SoapBottleStackData(SoapBottleKind hostKind) {
        this.hostKind = hostKind;
    }

    public SoapBottleKind hostKind() {
        return hostKind;
    }

    /** 已占用槽数量（非稠密长度）。 */
    public int layerCount() {
        int n = 0;
        for (SoapBottleLayer slot : slots) {
            if (slot != null) {
                n++;
            }
        }
        return n;
    }

    public int maxSlotsForContent() {
        return SoapBottleStackRules.maxSlotIndexExclusive(this);
    }

    /** 占用层按槽号升序（仅非空）；兼容旧「稠密列表」遍历。 */
    public List<SoapBottleLayer> layersView() {
        List<SoapBottleLayer> list = new ArrayList<>(layerCount());
        for (SoapBottleLayer slot : slots) {
            if (slot != null) {
                list.add(slot);
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Nullable
    public SoapBottleLayer slotAt(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS) {
            return null;
        }
        return slots[slotIndex];
    }

    public boolean hasSparseHoles() {
        int occupied = layerCount();
        if (occupied <= 1) {
            return false;
        }
        int highest = highestOccupiedSlot();
        return occupied < highest + 1;
    }

    public int highestOccupiedSlot() {
        for (int i = MAX_SLOTS - 1; i >= 0; i--) {
            if (slots[i] != null) {
                return i;
            }
        }
        return -1;
    }

    public int firstOccupiedSlot() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slots[i] != null) {
                return i;
            }
        }
        return -1;
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

    public boolean carrierHasSoap() {
        return carrierHasSoap && carrierSoap != null;
    }

    @Nullable
    public SoapBarAppearance carrierSoap() {
        return carrierSoap;
    }

    public boolean carrierBoxOpen() {
        return carrierBoxOpen;
    }

    public void setCarrierSoap(@Nullable SoapBarAppearance soap) {
        if (soap == null) {
            carrierHasSoap = false;
            carrierSoap = null;
        } else {
            carrierHasSoap = true;
            carrierSoap = soap;
        }
    }

    public void setCarrierBoxOpen(boolean open) {
        carrierBoxOpen = open;
    }

    /**
     * 槽位材质；空槽时返回 {@link SoapBottleKind#defaultMaterial()} 仅作占位——渲染应先查 {@link #slotAt}。
     */
    public SoapBottleLayer layerAt(int slotIndex) {
        SoapBottleLayer layer = slotAt(slotIndex);
        if (layer != null) {
            return layer;
        }
        return new SoapBottleLayer(hostKind, hostKind.defaultMaterial());
    }

    @Nullable
    public SoapBottleLayer topLayer() {
        int h = highestOccupiedSlot();
        return h < 0 ? null : slots[h];
    }

    public int topMaterial() {
        SoapBottleLayer top = topLayer();
        if (top != null) {
            return top.materialId();
        }
        int first = firstOccupiedSlot();
        if (first >= 0) {
            return slots[first].materialId();
        }
        return hostKind.defaultMaterial();
    }

    /** 供方块状态镜像：优先最低占用槽材质（单瓶残留时稳定）。 */
    public int displayMaterial() {
        int first = firstOccupiedSlot();
        if (first >= 0) {
            return slots[first].materialId();
        }
        return hostKind.defaultMaterial();
    }

    public boolean replaceTopMaterial(int materialId) {
        int h = highestOccupiedSlot();
        if (h < 0) {
            return false;
        }
        SoapBottleLayer top = slots[h];
        if (!top.kind().isValidMaterial(materialId)) {
            return false;
        }
        slots[h] = new SoapBottleLayer(top.kind(), materialId);
        return true;
    }

    public void setSingleLayer(SoapBottleKind kind, int materialId) {
        clearSlots();
        clearCarrierFields();
        slots[0] = new SoapBottleLayer(kind, materialId);
    }

    public boolean pushLayer(SoapBottleLayer layer) {
        if (!SoapBottleStackRules.canAcceptBottle(this, layer.kind())) {
            return false;
        }
        if (carrier != null && carrierIntermediate && layer.kind() == SoapBottleKind.BODY_CREAM) {
            return tryPushCreamAfterCarrier(layer.materialId());
        }
        int slot = firstEmptySlot(layer.kind());
        if (slot < 0) {
            return false;
        }
        slots[slot] = layer;
        return true;
    }

    public boolean tryPushCarrier(SoapStackCarrierKind kind) {
        return tryPushCarrier(kind, SoapBoxAppearance.DEFAULT_MATERIAL);
    }

    public boolean tryPushCarrier(SoapStackCarrierKind kind, int boxMaterialId) {
        if (!SoapBottleStackRules.canAcceptCarrier(this, kind)) {
            return false;
        }
        boolean intermediate = layerCount() == 2;
        this.carrier = kind;
        this.carrierIntermediate = intermediate;
        this.carrierBoxMaterialId =
                kind == SoapStackCarrierKind.BOX
                        ? boxMaterialId
                        : SoapBoxAppearance.DEFAULT_MATERIAL;
        this.carrierBoxOpen = false;
        this.carrierHasSoap = false;
        this.carrierSoap = null;
        return true;
    }

    /** 中间态再放乳霜 → 写入槽 2（第 3 陈列位），载体转完成态。 */
    public boolean tryPushCreamAfterCarrier(int materialId) {
        if (carrier == null || !carrierIntermediate) {
            return false;
        }
        if (layerCount() != 2 || slots[2] != null) {
            return false;
        }
        slots[2] = new SoapBottleLayer(SoapBottleKind.BODY_CREAM, materialId);
        carrierIntermediate = false;
        return true;
    }

    @Nullable
    public ItemStack popCarrierItem() {
        if (carrier == null) {
            return null;
        }
        ItemStack drop = carrier.toItemStack(carrierBoxMaterialId);
        // 盒/架物品暂不把内皂写入 NBT（与独立方块取出载体一致：皂另掉）
        clearCarrierFields();
        return drop;
    }

    @Nullable
    public SoapBarAppearance popCarrierSoap() {
        if (!carrierHasSoap || carrierSoap == null) {
            return null;
        }
        SoapBarAppearance soap = carrierSoap;
        carrierHasSoap = false;
        carrierSoap = null;
        return soap;
    }

    @Nullable
    public SoapBottleLayer popTopLayer() {
        int h = highestOccupiedSlot();
        if (h < 0) {
            return null;
        }
        return popLayerAt(h);
    }

    /** 清空指定槽（不挤位）；若导致载体态非法则清除载体。 */
    @Nullable
    public SoapBottleLayer popLayerAt(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_SLOTS || slots[slotIndex] == null) {
            return null;
        }
        SoapBottleLayer removed = slots[slotIndex];
        slots[slotIndex] = null;
        sanitizeCarrierAfterBottleChange();
        return removed;
    }

    private int firstEmptySlot(SoapBottleKind incoming) {
        int limit = SoapBottleStackRules.maxSlotIndexExclusiveForPush(this, incoming);
        for (int i = 0; i < limit; i++) {
            if (slots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private void sanitizeCarrierAfterBottleChange() {
        if (carrier == null) {
            return;
        }
        if (carrierIntermediate) {
            if (layerCount() != 2) {
                clearCarrierFields();
            }
            return;
        }
        // 完成态：须仍占 3 瓶且槽 2 为乳霜
        if (layerCount() != 3
                || slots[2] == null
                || slots[2].kind() != SoapBottleKind.BODY_CREAM) {
            clearCarrierFields();
        }
    }

    public void clear() {
        clearSlots();
        clearCarrierFields();
    }

    private void clearSlots() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = null;
        }
    }

    private void clearCarrierFields() {
        carrier = null;
        carrierIntermediate = false;
        carrierBoxMaterialId = SoapBoxAppearance.DEFAULT_MATERIAL;
        carrierHasSoap = false;
        carrierSoap = null;
        carrierBoxOpen = false;
    }

    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < MAX_SLOTS; i++) {
            SoapBottleLayer layer = slots[i];
            if (layer == null) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
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
                tag.putBoolean(TAG_CARRIER_BOX_OPEN, carrierBoxOpen);
            }
            tag.putBoolean(TAG_CARRIER_HAS_SOAP, carrierHasSoap);
            if (carrierHasSoap && carrierSoap != null) {
                ItemStack soapStack =
                        SoapBarBlockItem.stackWithAppearance(
                                ModBlocks.SOAP_BAR.item().get(), carrierSoap);
                if (soapStack.getTag() != null) {
                    tag.put(TAG_CARRIER_SOAP, soapStack.getTag().copy());
                }
            }
        } else {
            tag.remove(TAG_CARRIER_KIND);
            tag.remove(TAG_CARRIER_INTERMEDIATE);
            tag.remove(TAG_CARRIER_BOX_MAT);
            tag.remove(TAG_CARRIER_HAS_SOAP);
            tag.remove(TAG_CARRIER_SOAP);
            tag.remove(TAG_CARRIER_BOX_OPEN);
        }
    }

    public void load(CompoundTag tag, int legacyMaxStack) {
        clearSlots();
        clearCarrierFields();
        if (tag.contains(TAG_LAYERS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LAYERS, Tag.TAG_COMPOUND);
            boolean anySlot = false;
            int dense = 0;
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                SoapBottleKind kind = parseKind(entry.getString("Kind"));
                int mat = entry.getInt("Mat");
                if (!kind.isValidMaterial(mat)) {
                    continue;
                }
                SoapBottleLayer layer = new SoapBottleLayer(kind, mat);
                if (entry.contains("Slot")) {
                    int slot = entry.getInt("Slot");
                    if (slot >= 0 && slot < MAX_SLOTS) {
                        slots[slot] = layer;
                        anySlot = true;
                    }
                } else {
                    // 旧稠密存档：按序填入 0,1,2…
                    if (dense < MAX_SLOTS) {
                        slots[dense++] = layer;
                        anySlot = true;
                    }
                }
            }
            loadCarrier(tag);
            if (anySlot) {
                return;
            }
        }
        if (tag.contains(TAG_LAYER_MATS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LAYER_MATS, Tag.TAG_COMPOUND);
            int dense = 0;
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                int mat = entry.getInt("Mat");
                if (hostKind.isValidMaterial(mat) && dense < MAX_SLOTS) {
                    slots[dense++] = new SoapBottleLayer(hostKind, mat);
                }
            }
            loadCarrier(tag);
            if (dense > 0) {
                return;
            }
        }
        int legacy = tag.contains(TAG_LAYERS_LEGACY) ? tag.getInt(TAG_LAYERS_LEGACY) : 1;
        int count = Math.max(1, Math.min(legacyMaxStack, legacy));
        for (int i = 0; i < count && i < MAX_SLOTS; i++) {
            slots[i] = new SoapBottleLayer(hostKind, hostKind.defaultMaterial());
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
            carrierBoxOpen = tag.getBoolean(TAG_CARRIER_BOX_OPEN);
        }
        carrierHasSoap = tag.getBoolean(TAG_CARRIER_HAS_SOAP);
        if (carrierHasSoap && tag.contains(TAG_CARRIER_SOAP, Tag.TAG_COMPOUND)) {
            ItemStack soapStack = new ItemStack(ModBlocks.SOAP_BAR.item().get());
            soapStack.setTag(tag.getCompound(TAG_CARRIER_SOAP).copy());
            carrierSoap = SoapBarAppearance.fromStack(soapStack);
        } else {
            carrierHasSoap = false;
            carrierSoap = null;
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
