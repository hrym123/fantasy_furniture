package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;

/** 肥皂盒外观：盒体六色（方块状态 / 物品 NBT）；开盖态仅已放置方块。 */
public record SoapBoxAppearance(int boxMaterialId) {

    public static final int DEFAULT_MATERIAL = SoapBarAppearance.DEFAULT_MATERIAL;

    private static final String NBT_BOX_MAT = "BoxMat";

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public SoapBoxAppearance {
        if (!SoapBarMaterials.isValid(boxMaterialId)) {
            boxMaterialId = DEFAULT_MATERIAL;
        }
    }

    public static SoapBoxAppearance defaults() {
        return new SoapBoxAppearance(DEFAULT_MATERIAL);
    }

    public String boxGeoBasename(boolean open) {
        return open ? "soap_box_open" : "soap_box";
    }

    public String boxTextureBasename(boolean open) {
        return (open ? "soap_box_open_" : "soap_box_") + boxMaterialId;
    }

    public ResourceLocation boxModelLocation(boolean open) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + boxGeoBasename(open) + ".geo.json");
    }

    public ResourceLocation boxTextureLocation(boolean open) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + boxTextureBasename(open) + ".png");
    }

    public ResourceLocation animationLocation() {
        return STATIC_ANIMATION;
    }

    /** 物品 / 关盖预览：始终用关盖 geo 与贴图。 */
    public ResourceLocation closedItemModelLocation() {
        return boxModelLocation(false);
    }

    public ResourceLocation closedItemTextureLocation() {
        return boxTextureLocation(false);
    }

    public static SoapBoxAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof SoapBoxBlock) {
            return new SoapBoxAppearance(state.getValue(SoapBoxBlock.MATERIAL));
        }
        return defaults();
    }

    public static SoapBoxAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_BOX_MAT)) {
            return defaults();
        }
        return new SoapBoxAppearance(tag.getInt(NBT_BOX_MAT));
    }

    public static void writeToStack(ItemStack stack, SoapBoxAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_BOX_MAT, appearance.boxMaterialId());
    }
}
