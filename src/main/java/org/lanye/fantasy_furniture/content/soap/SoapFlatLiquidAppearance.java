package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 单材质图液体原料（沐浴液 / 洗发液）：颜料存 NBT {@code LiqMat}。 */
public record SoapFlatLiquidAppearance(int materialId, String textureStem) {

    private static final String NBT_LIQ_MAT = "LiqMat";

    public SoapFlatLiquidAppearance {
        if (!SoapBarMaterials.isValid(materialId)) {
            materialId = SoapBarAppearance.DEFAULT_MATERIAL;
        }
    }

    public static SoapFlatLiquidAppearance defaults(String textureStem) {
        return new SoapFlatLiquidAppearance(SoapBarAppearance.DEFAULT_MATERIAL, textureStem);
    }

    public ResourceLocation textureLocation() {
        int id = materialId >= 1 && materialId <= SoapBarMaterials.COUNT ? materialId : 1;
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "item/" + textureStem + "_ui_" + id);
    }

    public static SoapFlatLiquidAppearance fromStack(ItemStack stack, String textureStem) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_LIQ_MAT)) {
            return defaults(textureStem);
        }
        return new SoapFlatLiquidAppearance(tag.getInt(NBT_LIQ_MAT), textureStem);
    }

    public static void writeToStack(ItemStack stack, SoapFlatLiquidAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_LIQ_MAT, appearance.materialId());
    }
}
