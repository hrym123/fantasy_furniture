package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** 瓶罐摞 BE 侧层列表：种类 + 颜料；兼容旧版仅 {@code Mat} 的存档。 */
public final class SoapBottleStackData {

    private static final String TAG_LAYERS = "BottleLayers";
    private static final String TAG_LAYER_MATS = "LayerMats";
    private static final String TAG_LAYERS_LEGACY = "Layers";

    private final SoapBottleKind hostKind;
    private final List<SoapBottleLayer> layers = new ArrayList<>();

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

    public void setSingleLayer(SoapBottleKind kind, int materialId) {
        layers.clear();
        layers.add(new SoapBottleLayer(kind, materialId));
    }

    public boolean pushLayer(SoapBottleLayer layer) {
        if (layers.size() >= SoapBottleStackRules.maxStackFor(layers, layer.kind())) {
            return false;
        }
        layers.add(layer);
        return true;
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
    }

    public void load(CompoundTag tag, int legacyMaxStack) {
        layers.clear();
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
            if (!layers.isEmpty()) {
                return;
            }
        }
        int legacy = tag.contains(TAG_LAYERS_LEGACY) ? tag.getInt(TAG_LAYERS_LEGACY) : 1;
        int count = Math.max(1, Math.min(legacyMaxStack, legacy));
        for (int i = 0; i < count; i++) {
            layers.add(new SoapBottleLayer(hostKind, hostKind.defaultMaterial()));
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
