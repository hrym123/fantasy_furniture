package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;

/**
 * 肥皂外观 = 磨损档 geo × 颜料贴图（见设计书 {@code 01-组件一览 · 肥皂本体外观}）。
 *
 * <p>贴图当前仅按颜料档 {@code soap_bar_{1..6}.png}；磨损只换 geo。磨损档专用贴图待素材重绘后再扩展
 * {@link #textureBasename()}。
 *
 * <p>套袋态：{@link #bagMaterialId()} {@code > 0} 表示已套包装袋，袋色与皂颜料无关。
 */
public record SoapBarAppearance(int wear, int materialId, int bagMaterialId) {

    public static final int DEFAULT_WEAR = 0;
    public static final int DEFAULT_MATERIAL = 1;

    private static final String NBT_WEAR = "SoapWear";
    private static final String NBT_MAT = "SoapMat";
    private static final String NBT_BAG_MAT = "BagMat";

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public SoapBarAppearance(int wear, int materialId) {
        this(wear, materialId, 0);
    }

    public SoapBarAppearance {
        wear = SoapBarWear.clamp(wear);
        if (!SoapBarMaterials.isValid(materialId)) {
            materialId = DEFAULT_MATERIAL;
        }
        if (bagMaterialId < 0 || bagMaterialId > SoapBarMaterials.COUNT) {
            bagMaterialId = 0;
        }
    }

    public SoapBarWear wearEnum() {
        return SoapBarWear.fromIndex(wear);
    }

    public boolean isPackaged() {
        return bagMaterialId > 0;
    }

    /** 未入水磨损、可放入肥皂盒 / 肥皂架。 */
    public boolean isFull() {
        return wear == DEFAULT_WEAR;
    }

    public String geoBasename() {
        return wearEnum().geoBasename();
    }

    /** 颜料贴图 basename，与磨损 geo 无关（{@code soap_bar_1} … {@code soap_bar_6}）。 */
    public String textureBasename() {
        return "soap_bar_" + materialId;
    }

    public String bagTextureBasename() {
        return "soap_paper_bag_" + bagMaterialId;
    }

    public ResourceLocation modelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + geoBasename() + ".geo.json");
    }

    public ResourceLocation bagModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_paper_bag.geo.json");
    }

    public ResourceLocation bagTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + bagTextureBasename() + ".png");
    }

    /**
     * 肥皂盒内叠层 geo（源自 {@code 肥皂盒_肥皂.bbmodel} 的 {@code soap_bar} 组）。
     * 贴图仍用 {@link #textureLocation()}；与地上 {@link #modelLocation()} 分离。
     */
    public ResourceLocation soapBoxInnerModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_box_inner_soap.geo.json");
    }

    /**
     * 肥皂架上叠层 geo（源自 {@code 肥皂架_肥皂.bbmodel} 的 {@code soap_bar} 组）。
     * 贴图仍用 {@link #textureLocation()}；与地上 {@link #modelLocation()} 分离。
     */
    public ResourceLocation soapRackInnerModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/soap_rack_inner_soap.geo.json");
    }

    public ResourceLocation textureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + textureBasename() + ".png");
    }

    public ResourceLocation animationLocation() {
        return STATIC_ANIMATION;
    }

    public static SoapBarAppearance defaults() {
        return new SoapBarAppearance(DEFAULT_WEAR, DEFAULT_MATERIAL);
    }

    public static SoapBarAppearance fromState(BlockState state) {
        if (state.getBlock() instanceof SoapBarBlock block) {
            int bagMat = state.getValue(block.PACKAGED) ? state.getValue(block.BAG_MATERIAL) : 0;
            return new SoapBarAppearance(
                    state.getValue(SoapBarBlock.WEAR), state.getValue(SoapBarBlock.MATERIAL), bagMat);
        }
        return defaults();
    }

    public static SoapBarAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return defaults();
        }
        int w = tag.contains(NBT_WEAR) ? tag.getInt(NBT_WEAR) : DEFAULT_WEAR;
        int m = tag.contains(NBT_MAT) ? tag.getInt(NBT_MAT) : DEFAULT_MATERIAL;
        int bag = tag.contains(NBT_BAG_MAT) ? tag.getInt(NBT_BAG_MAT) : 0;
        return new SoapBarAppearance(w, m, bag);
    }

    public static void writeToStack(ItemStack stack, SoapBarAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_WEAR, appearance.wear());
        tag.putInt(NBT_MAT, appearance.materialId());
        if (appearance.isPackaged()) {
            tag.putInt(NBT_BAG_MAT, appearance.bagMaterialId());
        } else {
            tag.remove(NBT_BAG_MAT);
        }
    }

    public ItemStack toStack(ItemStack base) {
        writeToStack(base, this);
        return base;
    }
}
