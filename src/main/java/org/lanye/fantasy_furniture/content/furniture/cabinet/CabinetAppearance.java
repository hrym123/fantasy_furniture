package org.lanye.fantasy_furniture.content.furniture.cabinet;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** 柜子物品 NBT：材质档 id。 */
public final class CabinetAppearance {

    public static final String TAG_MATERIAL = "CabinetMaterial";

    private final int materialId;

    public CabinetAppearance(int materialId) {
        this.materialId = materialId;
    }

    public int materialId() {
        return materialId;
    }

    public static CabinetAppearance defaults() {
        return new CabinetAppearance(CabinetMaterials.DEFAULT);
    }

    public static CabinetAppearance fromStack(ItemStack stack, CabinetKind kind) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_MATERIAL)) {
            return defaults();
        }
        return new CabinetAppearance(CabinetMaterials.clamp(kind, tag.getInt(TAG_MATERIAL)));
    }

    public static void writeToStack(ItemStack stack, CabinetAppearance appearance) {
        stack.getOrCreateTag().putInt(TAG_MATERIAL, appearance.materialId());
    }

    @Nullable
    public static Integer readMaterialId(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_MATERIAL)) {
            return null;
        }
        return tag.getInt(TAG_MATERIAL);
    }
}
