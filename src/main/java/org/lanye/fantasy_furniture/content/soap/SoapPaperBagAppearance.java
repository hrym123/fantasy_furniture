package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;

/** 包装袋外观：袋色存 NBT / 方块状态；手持与摞体 geo 分离。 */
public record SoapPaperBagAppearance(int bagMaterialId) {

    private static final String NBT_BAG_MAT = "BagMat";

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public SoapPaperBagAppearance {
        if (!SoapPaperBagMaterials.isValid(bagMaterialId)) {
            bagMaterialId = SoapPaperBagMaterials.DEFAULT;
        }
    }

    public static SoapPaperBagAppearance defaults() {
        return new SoapPaperBagAppearance(SoapPaperBagMaterials.DEFAULT);
    }

    public String handheldTextureBasename() {
        return "soap_paper_bag_" + bagMaterialId;
    }

    /** 物品栏 UI 图（源自 {@code 包装袋/物品材质/*.png}）。 */
    public String itemUiTextureBasename() {
        return "soap_paper_bag_ui_" + bagMaterialId;
    }

    public ResourceLocation itemUiTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/item/" + itemUiTextureBasename() + ".png");
    }

    public String stackTextureBasename() {
        return "soap_paper_bag_stack_" + bagMaterialId;
    }

    /** 手持空袋与地上单只空袋（源自 {@code 包装袋_肥皂.bbmodel} 袋体）。 */
    public ResourceLocation handheldModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_bag.geo.json");
    }

    /** 地上仅 1 层时使用，与 {@link #handheldModelLocation()} 同源。 */
    public ResourceLocation placedSingleModelLocation() {
        return handheldModelLocation();
    }

    public ResourceLocation stackModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_bag_stack.geo.json");
    }

    public ResourceLocation handheldTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + handheldTextureBasename() + ".png");
    }

    public ResourceLocation stackTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + stackTextureBasename() + ".png");
    }

    public ResourceLocation animationLocation() {
        return STATIC_ANIMATION;
    }

    public static SoapPaperBagAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof SoapPaperBagBlock block) {
            return new SoapPaperBagAppearance(state.getValue(block.MATERIAL));
        }
        return defaults();
    }

    public static SoapPaperBagAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_BAG_MAT)) {
            return defaults();
        }
        return new SoapPaperBagAppearance(tag.getInt(NBT_BAG_MAT));
    }

    public static void writeToStack(ItemStack stack, SoapPaperBagAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_BAG_MAT, appearance.bagMaterialId());
    }
}
