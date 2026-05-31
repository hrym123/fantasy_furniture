package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;

/** 乳霜 Geo / 贴图路径；堆叠模型 {@code block1}…{@code block5} 对应陈列位 1…5（与 {@code 乳霜_堆叠_x5.bbmodel} 一致）。 */
public final class BodyCreamAssets {

    public static final int MAX_STACK = 5;

    /** 自底向上层 0…4 对应骨骼 {@code block1}…{@code block5}。 */
    public static final String[] STACK_LAYER_BONES =
            {"block1", "block2", "block3", "block4", "block5"};

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

    /** {@code block1}→1 … {@code block5}→5；非堆叠骨骼返回 {@code -1}。 */
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

    public static String stackBoneForLayerIndex(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= STACK_LAYER_BONES.length) {
            return null;
        }
        return STACK_LAYER_BONES[indexFromBottom];
    }

    /** 堆叠模型中，陈列位 {@code slot}（1…{@link #MAX_STACK}）是否应显示。 */
    public static boolean isStackSlotVisible(int slot, int visibleCount) {
        return slot >= 1 && slot <= visibleCount;
    }
}
