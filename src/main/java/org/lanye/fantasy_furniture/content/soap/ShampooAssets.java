package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 洗发露 Geo / 贴图路径；堆叠模型 {@code block1}…{@code block4} 对应陈列位 1…4。 */
public final class ShampooAssets {

    public static final int MAX_STACK = 4;

    private static final String BONE_PREFIX = "block";

    private ShampooAssets() {}

    public static ResourceLocation singleModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/shampoo.geo.json");
    }

    public static ResourceLocation stackModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/shampoo_stack.geo.json");
    }

    public static ResourceLocation textureLocation(int materialId) {
        int id = materialId >= 1 && materialId <= SoapBarMaterials.COUNT ? materialId : 1;
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/shampoo_" + id + ".png");
    }

    public static ResourceLocation singleAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/shampoo.animation.json");
    }

    public static ResourceLocation stackAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/shampoo_stack.animation.json");
    }

    /** {@code block1}→1 … {@code block4}→4；非堆叠骨骼返回 {@code -1}。 */
    public static int slotIndexFromBoneName(String boneName) {
        if (boneName == null || !boneName.startsWith(BONE_PREFIX)) {
            return -1;
        }
        try {
            int n = Integer.parseInt(boneName.substring(BONE_PREFIX.length()));
            return n >= 1 && n <= MAX_STACK ? n : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /** 堆叠模型中，陈列位 {@code slot}（1…{@link #MAX_STACK}）是否应显示。 */
    public static boolean isStackSlotVisible(int slot, int visibleCount) {
        return slot >= 1 && slot <= visibleCount;
    }
}
