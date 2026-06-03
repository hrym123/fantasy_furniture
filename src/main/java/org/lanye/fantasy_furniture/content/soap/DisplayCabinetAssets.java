package org.lanye.fantasy_furniture.content.soap;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;

/** 陈列柜 Geo / 贴图；关盒 / 打开与瓶罐摆放变体见设计书 {@code display_cabinet}。 */
public final class DisplayCabinetAssets {

    public static final int MAX_BOTTLES = 2;

    private static final ResourceLocation STATIC_ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");

    private DisplayCabinetAssets() {}

    public static ResourceLocation closedModelLocation() {
        return geo("display_cabinet");
    }

    public static ResourceLocation openModelLocation() {
        return geo("display_cabinet_open");
    }

    /**
     * moonstarfish {@code 陈列柜_沐浴露x2.bbmodel} → {@code display_cabinet_wash2.geo.json}。
     * geo 内 {@code texture_width/height} 须为 32（与 {@code body_wash_*.png} 一致；Blockbench 组合模型
     * 导出默认为 64，陈列柜 overlay 贴单瓶材质前需改回 32）。
     */
    public static ResourceLocation wash2ModelLocation() {
        return geo("display_cabinet_wash2");
    }

    /**
     * moonstarfish {@code 陈列柜_洗发露x2.bbmodel} → {@code display_cabinet_shampoo2.geo.json}。
     * 贴图尺寸约定同 {@link #wash2ModelLocation()}（{@code shampoo_*.png} 为 32×32）。
     */
    public static ResourceLocation shampoo2ModelLocation() {
        return geo("display_cabinet_shampoo2");
    }

    /** 柜体 shell：开 / 关均只用空壳 geo；瓶罐 overlay 用 {@link #wash2ModelLocation()} / {@link #shampoo2ModelLocation()}。 */
    public static ResourceLocation modelLocation(DisplayCabinetBlockEntity entity) {
        return entity.isOpen() ? openModelLocation() : closedModelLocation();
    }

    public static ResourceLocation textureLocation(boolean open, int boxMaterialId) {
        int id = SoapPaperBoxMaterials.isValid(boxMaterialId) ? boxMaterialId : SoapPaperBoxMaterials.DEFAULT;
        String stem = open ? "display_cabinet_open_" : "display_cabinet_";
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + stem + id + ".png");
    }

    public static ResourceLocation animationLocation() {
        return STATIC_ANIMATION;
    }

    /**
     * 陈列柜 ×2 bbmodel 中，指定槽位应显示的瓶罐骨骼（1 号位 = {@code slotIndex} 0）。
     *
     * <p>沐浴露：{@code group5}/{@code group6}/{@code bone}、{@code group7}/{@code group8}/{@code bone2}；
     * 洗发露：{@code group9}/{@code group10}、{@code group7}/{@code group8}。
     */
    public static Set<String> slotBottleBones(DisplayCabinetBottleKind kind, int slotIndex) {
        return switch (kind) {
            case BODY_WASH ->
                    switch (slotIndex) {
                        case 0 -> Set.of("group5", "group6", "bone");
                        case 1 -> Set.of("group7", "group8", "bone2");
                        default -> Set.of();
                    };
            case SHAMPOO ->
                    switch (slotIndex) {
                        case 0 -> Set.of("group9", "group10");
                        case 1 -> Set.of("group7", "group8");
                        default -> Set.of();
                    };
        };
    }

    private static ResourceLocation geo(String basename) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + basename + ".geo.json");
    }
}
