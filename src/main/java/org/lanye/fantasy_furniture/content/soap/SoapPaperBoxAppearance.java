package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;

/** 包装盒外观：盒色存 NBT / 方块状态 {@link SoapPaperBoxBlock#MATERIAL}。 */
public record SoapPaperBoxAppearance(int materialId) {

    private static final String NBT_BOX_MAT = "BoxMat";

    public SoapPaperBoxAppearance {
        if (!SoapPaperBoxMaterials.isValid(materialId)) {
            materialId = SoapPaperBoxMaterials.DEFAULT;
        }
    }

    public static SoapPaperBoxAppearance defaults() {
        return new SoapPaperBoxAppearance(SoapPaperBoxMaterials.DEFAULT);
    }

    public ResourceLocation textureLocation() {
        return SoapPaperBoxAssets.textureLocation(materialId);
    }

    /** 物品栏 UI 图（源自 {@code 包装盒/物品材质/*.png}）。 */
    public String itemUiTextureBasename() {
        return "soap_paper_box_ui_" + materialId;
    }

    public ResourceLocation itemUiTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/item/" + itemUiTextureBasename() + ".png");
    }

    public static SoapPaperBoxAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof SoapPaperBoxBlock block) {
            return new SoapPaperBoxAppearance(state.getValue(block.MATERIAL));
        }
        return defaults();
    }

    public static SoapPaperBoxAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_BOX_MAT)) {
            return defaults();
        }
        return new SoapPaperBoxAppearance(tag.getInt(NBT_BOX_MAT));
    }

    public static void writeToStack(ItemStack stack, SoapPaperBoxAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_BOX_MAT, appearance.materialId());
    }
}
