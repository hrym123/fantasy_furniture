package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;

/** 沐浴露外观：颜料存 NBT / 方块状态 {@link BodyWashBlock#MATERIAL}。 */
public record BodyWashAppearance(int materialId) {

    private static final String NBT_WASH_MAT = "WashMat";

    public BodyWashAppearance {
        if (!BodyWashMaterials.isValid(materialId)) {
            materialId = BodyWashMaterials.DEFAULT;
        }
    }

    public static BodyWashAppearance defaults() {
        return new BodyWashAppearance(BodyWashMaterials.DEFAULT);
    }

    public ResourceLocation textureLocation() {
        return BodyWashAssets.textureLocation(materialId);
    }

    public static BodyWashAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof BodyWashBlock block) {
            return new BodyWashAppearance(state.getValue(block.MATERIAL));
        }
        return defaults();
    }

    public static BodyWashAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_WASH_MAT)) {
            return defaults();
        }
        return new BodyWashAppearance(tag.getInt(NBT_WASH_MAT));
    }

    public static void writeToStack(ItemStack stack, BodyWashAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_WASH_MAT, appearance.materialId());
    }
}
