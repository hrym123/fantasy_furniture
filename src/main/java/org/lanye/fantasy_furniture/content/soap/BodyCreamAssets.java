package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;

/** 乳霜 Geo / 贴图路径；堆叠模型 {@code block2}…{@code block6} 对应陈列位 1…5。 */
public final class BodyCreamAssets {

    public static final int MAX_STACK = 5;

    private static final String BONE_PREFIX = "block";

    private BodyCreamAssets() {}

    public static ResourceLocation singleModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/body_cream.geo.json");
    }

    public static ResourceLocation stackModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "geo/block/body_cream_stack.geo.json");
    }

    public static ResourceLocation textureLocation(int materialId) {
        int id = materialId >= 1 && materialId <= SoapBarMaterials.COUNT ? materialId : 1;
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/body_cream_" + id + ".png");
    }

    public static ResourceLocation singleAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/body_cream.animation.json");
    }

    public static ResourceLocation stackAnimationLocation() {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "animations/block/body_cream_stack.animation.json");
    }

    /** {@code block2}→1 … {@code block6}→5；非堆叠骨骼返回 {@code -1}。 */
    public static int slotIndexFromBoneName(String boneName) {
        if (boneName == null || !boneName.startsWith(BONE_PREFIX)) {
            return -1;
        }
        try {
            int n = Integer.parseInt(boneName.substring(BONE_PREFIX.length()));
            int slot = n - 1;
            return slot >= 1 && slot <= MAX_STACK ? slot : -1;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /** 堆叠模型中，陈列位 {@code slot}（1…{@link #MAX_STACK}）是否应显示。 */
    public static boolean isStackSlotVisible(int slot, int visibleCount) {
        return slot >= 1 && slot <= visibleCount;
    }
}
