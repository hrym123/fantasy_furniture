package org.lanye.fantasy_furniture.content.furniture.decor;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** 电脑物品 NBT：材质档 id（1～6）。 */
public final class ComputerAppearance {

    public static final String TAG_MATERIAL = "ComputerMaterial";

    private final int materialId;

    public ComputerAppearance(int materialId) {
        this.materialId = ComputerMaterials.clamp(materialId);
    }

    public int materialId() {
        return materialId;
    }

    public static ComputerAppearance defaults() {
        return new ComputerAppearance(ComputerMaterials.DEFAULT);
    }

    public static ComputerAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_MATERIAL)) {
            return defaults();
        }
        return new ComputerAppearance(tag.getInt(TAG_MATERIAL));
    }

    public static void writeToStack(ItemStack stack, ComputerAppearance appearance) {
        stack.getOrCreateTag().putInt(TAG_MATERIAL, appearance.materialId());
    }

    @Nullable
    public static Integer readMaterialId(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_MATERIAL)) {
            return null;
        }
        return ComputerMaterials.clamp(tag.getInt(TAG_MATERIAL));
    }
}
