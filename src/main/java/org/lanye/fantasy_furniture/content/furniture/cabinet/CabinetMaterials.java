package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 柜子材质档：柜子1 四色、柜子2 六色；贴图 {@code textures/block/{color}_{assetId}.png}。
 */
public final class CabinetMaterials {

    /** {@link org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock#MATERIAL} 上界。 */
    public static final int MAX_COUNT = 6;

    public static final int DEFAULT = 1;

    private static final String[] CABINET_1_COLORS = {"white", "brown", "blue", "green"};
    private static final String[] CABINET_2_COLORS = {
        "white", "brown", "blue", "green", "yellow", "pink"
    };

    private CabinetMaterials() {}

    public static int count(CabinetKind kind) {
        return kind == CabinetKind.CABINET_1 ? CABINET_1_COLORS.length : CABINET_2_COLORS.length;
    }

    public static boolean isValid(CabinetKind kind, int materialId) {
        return materialId >= 1 && materialId <= count(kind);
    }

    public static int clamp(CabinetKind kind, int materialId) {
        return isValid(kind, materialId) ? materialId : DEFAULT;
    }

    public static String colorKey(CabinetKind kind, int materialId) {
        String[] keys = kind == CabinetKind.CABINET_1 ? CABINET_1_COLORS : CABINET_2_COLORS;
        return keys[clamp(kind, materialId) - 1];
    }

    public static String colorTranslationKey(CabinetKind kind, int materialId) {
        return "cabinet.fantasy_furniture.color." + colorKey(kind, materialId);
    }

    /** 方块 / 物品贴图：{@code {color}_{assetId}.png}。 */
    public static ResourceLocation textureLocation(CabinetKind kind, int materialId) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID,
                "textures/block/" + colorKey(kind, materialId) + "_" + kind.assetId() + ".png");
    }

    public static ResourceLocation itemGeoLocation(CabinetKind kind) {
        String stem = kind == CabinetKind.CABINET_1 ? "cabinet_1_cell" : kind.assetId();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/" + stem + ".geo.json");
    }

    public static ResourceLocation itemAnimationLocation(CabinetKind kind) {
        String stem = kind == CabinetKind.CABINET_1 ? "cabinet_1_cell" : kind.assetId();
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/" + stem + ".animation.json");
    }
}
