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
 * <p>贴图仅按颜料档 {@code soap_bar_{1..6}.png}；磨损只换 geo，三档共用同一套贴图（见 DEC-301）。
 *
 * <p>套袋态：{@link #bagMaterialId()} {@code > 0} 表示已套包装袋，袋色与皂颜料无关。
 * 撕开态：{@link #packagingTorn()} 为真时袋体 geo 换为 {@code soap_paper_bag_torn}。
 *
 * <p>{@link #particleMatId()}：制皂液体决定的入水粒子色（与 {@link #materialId()} 颜料无关）。
 */
public record SoapBarAppearance(
        int wear, int materialId, int bagMaterialId, boolean packagingTorn, int particleMatId) {

    public static final int DEFAULT_WEAR = 0;
    public static final int DEFAULT_MATERIAL = 1;
    public static final int DEFAULT_PARTICLE_MAT = 1;

    private static final String NBT_WEAR = "SoapWear";
    private static final String NBT_MAT = "SoapMat";
    private static final String NBT_BAG_MAT = "BagMat";
    private static final String NBT_BAG_TORN = "BagTorn";
    private static final String NBT_PART_MAT = "PartMat";

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public SoapBarAppearance(int wear, int materialId) {
        this(wear, materialId, 0, false, DEFAULT_PARTICLE_MAT);
    }

    public SoapBarAppearance(int wear, int materialId, int bagMaterialId) {
        this(wear, materialId, bagMaterialId, false, DEFAULT_PARTICLE_MAT);
    }

    public SoapBarAppearance(int wear, int materialId, int bagMaterialId, boolean packagingTorn) {
        this(wear, materialId, bagMaterialId, packagingTorn, DEFAULT_PARTICLE_MAT);
    }

    public SoapBarAppearance {
        wear = SoapBarWear.clamp(wear);
        if (!SoapBarMaterials.isValid(materialId)) {
            materialId = DEFAULT_MATERIAL;
        }
        if (bagMaterialId < 0 || bagMaterialId > SoapBarMaterials.COUNT) {
            bagMaterialId = 0;
        }
        if (!SoapFlatLiquidMaterials.isValid(particleMatId)) {
            particleMatId = DEFAULT_PARTICLE_MAT;
        }
        if (!isPackaged(bagMaterialId)) {
            packagingTorn = false;
        }
    }

    private static boolean isPackaged(int bagMaterialId) {
        return bagMaterialId > 0;
    }

    public SoapBarWear wearEnum() {
        return SoapBarWear.fromIndex(wear);
    }

    public boolean isPackaged() {
        return isPackaged(bagMaterialId);
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

    /** 物品栏 UI 图（源自 {@code 肥皂/物品材质/肥皂_物品材质_{色名}.png}）。 */
    public String itemUiTextureBasename() {
        return "soap_bar_ui_" + materialId;
    }

    public ResourceLocation itemUiTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/item/" + itemUiTextureBasename() + ".png");
    }

    public String bagTextureBasename() {
        return "soap_paper_bag_" + bagMaterialId;
    }

    public ResourceLocation modelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + geoBasename() + ".geo.json");
    }

    public ResourceLocation bagModelLocation() {
        return packagingTorn
                ? SoapPackagingAssets.bagTornModelLocation()
                : SoapPackagingAssets.bagIntactModelLocation();
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
            boolean torn = state.getValue(block.PACKAGED) && state.getValue(block.PACKAGING_TORN);
            return new SoapBarAppearance(
                    state.getValue(SoapBarBlock.WEAR),
                    state.getValue(SoapBarBlock.MATERIAL),
                    bagMat,
                    torn,
                    DEFAULT_PARTICLE_MAT);
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
        boolean torn = bag > 0 && tag.getBoolean(NBT_BAG_TORN);
        int part =
                tag.contains(NBT_PART_MAT) ? tag.getInt(NBT_PART_MAT) : DEFAULT_PARTICLE_MAT;
        return new SoapBarAppearance(w, m, bag, torn, part);
    }

    public static void writeToStack(ItemStack stack, SoapBarAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_WEAR, appearance.wear());
        tag.putInt(NBT_MAT, appearance.materialId());
        if (appearance.particleMatId() != DEFAULT_PARTICLE_MAT) {
            tag.putInt(NBT_PART_MAT, appearance.particleMatId());
        } else {
            tag.remove(NBT_PART_MAT);
        }
        if (appearance.isPackaged()) {
            tag.putInt(NBT_BAG_MAT, appearance.bagMaterialId());
            if (appearance.packagingTorn()) {
                tag.putBoolean(NBT_BAG_TORN, true);
            } else {
                tag.remove(NBT_BAG_TORN);
            }
        } else {
            tag.remove(NBT_BAG_MAT);
            tag.remove(NBT_BAG_TORN);
        }
    }

    public ItemStack toStack(ItemStack base) {
        writeToStack(base, this);
        return base;
    }
}
