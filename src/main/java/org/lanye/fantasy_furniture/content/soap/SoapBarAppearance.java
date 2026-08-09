package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;

/**
 * 肥皂外观 = 耐久度档 geo × 颜料贴图（见设计书 soap_bar 玩法 · 耐久度）。
 *
 * <p>贴图仅按颜料档 {@code soap_bar_{1..6}.png}；耐久度只换 geo，三档共用同一套贴图（见 DEC-301）。
 *
 * <p>包装态：{@link #bagMaterialId()} 与 {@link #boxMaterialId()} 互斥，{@code > 0} 表示已套袋或套盒；
 * 包装色与皂颜料无关。撕开态：{@link #packagingTorn()} 为真时换 torn geo。
 *
 * <p>{@link #particleMatId()}：制皂液体决定的入水粒子色（与 {@link #materialId()} 颜料无关）。
 *
 * <p>{@link #durability()}：剩余耐久 3/2/1（最大 {@link SoapBarDurability#MAX}）。旧 NBT {@code SoapWear}
 * 在读取时迁移。
 */
public record SoapBarAppearance(
        int durability,
        int materialId,
        int bagMaterialId,
        boolean packagingTorn,
        int particleMatId,
        int boxMaterialId) {

    public static final int DEFAULT_DURABILITY = SoapBarDurability.MAX;
    public static final int DEFAULT_MATERIAL = 1;
    public static final int DEFAULT_PARTICLE_MAT = 1;

    private static final String NBT_DURABILITY = "SoapDurability";
    /** 旧键：0/1/2 磨损档；读取时转为剩余耐久。 */
    private static final String NBT_WEAR_LEGACY = "SoapWear";
    private static final String NBT_MAT = "SoapMat";
    private static final String NBT_BAG_MAT = "BagMat";
    private static final String NBT_BOX_MAT = "BoxMat";
    private static final String NBT_BAG_TORN = "BagTorn";
    private static final String NBT_PART_MAT = "PartMat";
    /** 为 true 时 {@link #NBT_PART_MAT} 来自制皂液体，可与颜料不同；缺省/创造栏皂按颜料映射。 */
    public static final String NBT_PART_FROM_LIQUID = "PartFromLiquid";

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    public SoapBarAppearance(int durability, int materialId) {
        this(durability, materialId, 0, false, pigmentToParticleMat(materialId), 0);
    }

    public SoapBarAppearance(int durability, int materialId, int bagMaterialId) {
        this(durability, materialId, bagMaterialId, false, pigmentToParticleMat(materialId), 0);
    }

    public SoapBarAppearance(
            int durability, int materialId, int bagMaterialId, boolean packagingTorn) {
        this(durability, materialId, bagMaterialId, packagingTorn, pigmentToParticleMat(materialId), 0);
    }

    public SoapBarAppearance(
            int durability,
            int materialId,
            int bagMaterialId,
            boolean packagingTorn,
            int particleMatId) {
        this(durability, materialId, bagMaterialId, packagingTorn, particleMatId, 0);
    }

    /** 套盒皂（袋色恒为 0）。 */
    public static SoapBarAppearance withBox(
            int durability,
            int materialId,
            int boxMaterialId,
            boolean packagingTorn,
            int particleMatId) {
        return new SoapBarAppearance(
                durability, materialId, 0, packagingTorn, particleMatId, boxMaterialId);
    }

    public SoapBarAppearance {
        durability = SoapBarDurability.clamp(durability);
        if (!SoapBarMaterials.isValid(materialId)) {
            materialId = DEFAULT_MATERIAL;
        }
        if (bagMaterialId < 0 || bagMaterialId > SoapBarMaterials.COUNT) {
            bagMaterialId = 0;
        }
        if (boxMaterialId < 0 || boxMaterialId > SoapPaperBoxMaterials.COUNT) {
            boxMaterialId = 0;
        }
        if (boxMaterialId > 0) {
            bagMaterialId = 0;
        }
        if (!SoapFlatLiquidMaterials.isValid(particleMatId)) {
            particleMatId = DEFAULT_PARTICLE_MAT;
        }
        if (!isPackaged(bagMaterialId, boxMaterialId)) {
            packagingTorn = false;
        }
    }

    private static boolean isPackaged(int bagMaterialId, int boxMaterialId) {
        return bagMaterialId > 0 || boxMaterialId > 0;
    }

    public SoapBarDurability durabilityEnum() {
        return SoapBarDurability.fromRemaining(durability);
    }

    public boolean isBagged() {
        return bagMaterialId > 0;
    }

    public boolean isBoxed() {
        return boxMaterialId > 0;
    }

    public boolean isPackaged() {
        return isPackaged(bagMaterialId, boxMaterialId);
    }

    /** 当前包装色（袋或盒）；未包装为 0。 */
    public int packagingMaterialId() {
        if (isBoxed()) {
            return boxMaterialId;
        }
        return bagMaterialId;
    }

    /** 满耐久（未入水消耗），可放入肥皂盒 / 肥皂架。 */
    public boolean isFull() {
        return durability == DEFAULT_DURABILITY;
    }

    public String geoBasename() {
        return durabilityEnum().geoBasename();
    }

    /** 颜料贴图 basename，与耐久度 geo 无关（{@code soap_bar_1} … {@code soap_bar_6}）。 */
    public String textureBasename() {
        return "soap_bar_" + materialId;
    }

    /** 物品栏 UI 图（源自 {@code 肥皂/物品材质/{色名}.png}，六色短名）。 */
    public String itemUiTextureBasename() {
        return "soap_bar_ui_" + materialId;
    }

    public ResourceLocation itemUiTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/item/" + itemUiTextureBasename() + ".png");
    }

    public String packagingTextureBasename() {
        if (isBoxed()) {
            return "soap_paper_box_" + boxMaterialId;
        }
        return "soap_paper_bag_" + bagMaterialId;
    }

    public String bagTextureBasename() {
        return packagingTextureBasename();
    }

    public ResourceLocation modelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + geoBasename() + ".geo.json");
    }

    /** 包装袋 / 包装盒 geo（完整或撕开）。 */
    public ResourceLocation bagModelLocation() {
        if (isBoxed()) {
            return packagingTorn
                    ? SoapPackagingAssets.boxTornModelLocation()
                    : SoapPackagingAssets.boxIntactModelLocation();
        }
        return packagingTorn
                ? SoapPackagingAssets.bagTornModelLocation()
                : SoapPackagingAssets.bagIntactModelLocation();
    }

    public ResourceLocation bagTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + packagingTextureBasename() + ".png");
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
        return new SoapBarAppearance(DEFAULT_DURABILITY, DEFAULT_MATERIAL);
    }

    /**
     * 皂体颜料 id → 入水粒子贴图 id（moonstarfish {@code 粒子_*.png} 按液体色名命名）。
     * 无显式 {@link #NBT_PART_MAT} 时（如创造栏六色皂）与颜料同色；制皂模具写入的 PartMat 仍优先。
     */
    public static int pigmentToParticleMat(int materialId) {
        if (!SoapBarMaterials.isValid(materialId)) {
            materialId = DEFAULT_MATERIAL;
        }
        return switch (materialId) {
            case 1 -> 5; // 冰蓝
            case 2 -> 4; // 薄荷绿
            case 3 -> 2; // 丁香紫
            case 4 -> 1; // 樱花粉
            case 5 -> 6; // 黄油黄
            case 6 -> 3; // 樱桃红
            default -> DEFAULT_PARTICLE_MAT;
        };
    }

    public static SoapBarAppearance fromState(BlockState state) {
        return fromState(state, 0);
    }

    public static SoapBarAppearance fromState(BlockState state, int particleMatId) {
        if (state.getBlock() instanceof SoapBarBlock block) {
            boolean packaged = state.getValue(block.PACKAGED);
            int pkgMat = packaged ? state.getValue(block.BAG_MATERIAL) : 0;
            boolean boxed = packaged && state.getValue(block.BOXED);
            boolean torn = packaged && state.getValue(block.PACKAGING_TORN);
            int material = state.getValue(SoapBarBlock.MATERIAL);
            int part = particleMatId > 0 ? particleMatId : pigmentToParticleMat(material);
            int bag = boxed ? 0 : pkgMat;
            int box = boxed ? pkgMat : 0;
            return new SoapBarAppearance(
                    state.getValue(SoapBarBlock.DURABILITY), material, bag, torn, part, box);
        }
        return defaults();
    }

    public static SoapBarAppearance fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return defaults();
        }
        int d = readDurability(tag);
        int m = tag.contains(NBT_MAT) ? tag.getInt(NBT_MAT) : DEFAULT_MATERIAL;
        int box = tag.contains(NBT_BOX_MAT) ? tag.getInt(NBT_BOX_MAT) : 0;
        int bag = tag.contains(NBT_BAG_MAT) ? tag.getInt(NBT_BAG_MAT) : 0;
        if (box > 0) {
            bag = 0;
        }
        boolean torn = (bag > 0 || box > 0) && tag.getBoolean(NBT_BAG_TORN);
        int part =
                tag.contains(NBT_PART_MAT) && tag.getBoolean(NBT_PART_FROM_LIQUID)
                        ? tag.getInt(NBT_PART_MAT)
                        : pigmentToParticleMat(m);
        return new SoapBarAppearance(d, m, bag, torn, part, box);
    }

    /** 优先 {@code SoapDurability}；否则旧 {@code SoapWear} 0/1/2 → 3/2/1。 */
    public static int readDurability(CompoundTag tag) {
        if (tag.contains(NBT_DURABILITY)) {
            return SoapBarDurability.clamp(tag.getInt(NBT_DURABILITY));
        }
        if (tag.contains(NBT_WEAR_LEGACY)) {
            return SoapBarDurability.fromLegacyWear(tag.getInt(NBT_WEAR_LEGACY));
        }
        return DEFAULT_DURABILITY;
    }

    public static void writeToStack(ItemStack stack, SoapBarAppearance appearance) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_DURABILITY, appearance.durability());
        tag.remove(NBT_WEAR_LEGACY);
        tag.putInt(NBT_MAT, appearance.materialId());
        tag.putInt(NBT_PART_MAT, appearance.particleMatId());
        // 不在此写入 PartFromLiquid；创造栏路径会清掉，模具在 write 后再 mark
        if (appearance.isBoxed()) {
            tag.putInt(NBT_BOX_MAT, appearance.boxMaterialId());
            tag.remove(NBT_BAG_MAT);
            if (appearance.packagingTorn()) {
                tag.putBoolean(NBT_BAG_TORN, true);
            } else {
                tag.remove(NBT_BAG_TORN);
            }
        } else if (appearance.isBagged()) {
            tag.putInt(NBT_BAG_MAT, appearance.bagMaterialId());
            tag.remove(NBT_BOX_MAT);
            if (appearance.packagingTorn()) {
                tag.putBoolean(NBT_BAG_TORN, true);
            } else {
                tag.remove(NBT_BAG_TORN);
            }
        } else {
            tag.remove(NBT_BAG_MAT);
            tag.remove(NBT_BOX_MAT);
            tag.remove(NBT_BAG_TORN);
        }
    }

    /** 创造栏等：按颜料映射粒子，忽略历史错误 PartMat。 */
    public static void writeCreativeParticle(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_PART_FROM_LIQUID);
        int m = tag.contains(NBT_MAT) ? tag.getInt(NBT_MAT) : DEFAULT_MATERIAL;
        tag.putInt(NBT_PART_MAT, pigmentToParticleMat(m));
    }

    public static boolean isParticleFromLiquid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(NBT_PART_FROM_LIQUID);
    }

    /** 模具制皂：粒子色跟液体，与颜料可不同。 */
    public static void markParticleFromLiquid(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBT_PART_FROM_LIQUID, true);
    }

    public ItemStack toStack(ItemStack base) {
        writeToStack(base, this);
        return base;
    }
}
