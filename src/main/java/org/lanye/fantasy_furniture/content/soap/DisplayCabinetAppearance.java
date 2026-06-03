package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.block.DisplayCabinetBlock;

/** 陈列柜外观：包装盒色存 NBT / 方块状态 {@link DisplayCabinetBlock#MATERIAL}。 */
public record DisplayCabinetAppearance(int materialId) {

    private static final String NBT_BOX_MAT = "BoxMat";

    public DisplayCabinetAppearance {
        if (!SoapPaperBoxMaterials.isValid(materialId)) {
            materialId = SoapPaperBoxMaterials.DEFAULT;
        }
    }

    public static DisplayCabinetAppearance defaults() {
        return new DisplayCabinetAppearance(SoapPaperBoxMaterials.DEFAULT);
    }

    public ResourceLocation textureLocation(boolean open) {
        return DisplayCabinetAssets.textureLocation(open, materialId);
    }

    public static DisplayCabinetAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof DisplayCabinetBlock block) {
            return new DisplayCabinetAppearance(state.getValue(block.MATERIAL));
        }
        return defaults();
    }

    public static DisplayCabinetAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_BOX_MAT)) {
            return defaults();
        }
        return new DisplayCabinetAppearance(tag.getInt(NBT_BOX_MAT));
    }

    public static void writeToStack(ItemStack stack, DisplayCabinetAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_BOX_MAT, appearance.materialId());
    }
}
