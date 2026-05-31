package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;

/** 洗发露外观：颜料存 NBT / 方块状态 {@link ShampooBlock#MATERIAL}。 */
public record ShampooAppearance(int materialId) {

    private static final String NBT_WASH_MAT = "ShampooMat";

    public ShampooAppearance {
        if (!ShampooMaterials.isValid(materialId)) {
            materialId = ShampooMaterials.DEFAULT;
        }
    }

    public static ShampooAppearance defaults() {
        return new ShampooAppearance(ShampooMaterials.DEFAULT);
    }

    public ResourceLocation textureLocation() {
        return ShampooAssets.textureLocation(materialId);
    }

    public static ShampooAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof ShampooBlock block) {
            return new ShampooAppearance(state.getValue(block.MATERIAL));
        }
        return defaults();
    }

    public static ShampooAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_WASH_MAT)) {
            return defaults();
        }
        return new ShampooAppearance(tag.getInt(NBT_WASH_MAT));
    }

    public static void writeToStack(ItemStack stack, ShampooAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_WASH_MAT, appearance.materialId());
    }
}
