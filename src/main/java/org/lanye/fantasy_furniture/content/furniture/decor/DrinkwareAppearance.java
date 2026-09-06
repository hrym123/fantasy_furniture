package org.lanye.fantasy_furniture.content.furniture.decor;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** 杯具物品 NBT：材质档 id（1～9）。 */
public final class DrinkwareAppearance {

    public static final String TAG_MATERIAL = "DrinkwareMaterial";

    private final int materialId;

    public DrinkwareAppearance(int materialId) {
        this.materialId = DrinkwareMaterials.clamp(materialId);
    }

    public int materialId() {
        return materialId;
    }

    public static DrinkwareAppearance defaults() {
        return new DrinkwareAppearance(DrinkwareMaterials.DEFAULT);
    }

    public static DrinkwareAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_MATERIAL)) {
            return defaults();
        }
        return new DrinkwareAppearance(tag.getInt(TAG_MATERIAL));
    }

    public static void writeToStack(ItemStack stack, DrinkwareAppearance appearance) {
        stack.getOrCreateTag().putInt(TAG_MATERIAL, appearance.materialId());
    }

    @Nullable
    public static Integer readMaterialId(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_MATERIAL)) {
            return null;
        }
        return DrinkwareMaterials.clamp(tag.getInt(TAG_MATERIAL));
    }
}
