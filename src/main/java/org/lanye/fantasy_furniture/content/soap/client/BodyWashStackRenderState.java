package org.lanye.fantasy_furniture.content.soap.client;

import org.lanye.fantasy_furniture.content.soap.BodyWashMaterials;

/** 摞体渲染 Pass：可见骨骼名与颜料（线程局部）。 */
public final class BodyWashStackRenderState {

    private static final ThreadLocal<String> VISIBLE_BONE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> LAYER_MATERIAL = new ThreadLocal<>();

    private BodyWashStackRenderState() {}

    public static void set(String visibleBone, int layerMaterial) {
        VISIBLE_BONE.set(visibleBone);
        LAYER_MATERIAL.set(layerMaterial);
    }

    public static String visibleBone() {
        return VISIBLE_BONE.get();
    }

    public static int layerMaterial() {
        Integer mat = LAYER_MATERIAL.get();
        return mat != null ? mat : BodyWashMaterials.DEFAULT;
    }

    public static void clear() {
        VISIBLE_BONE.remove();
        LAYER_MATERIAL.remove();
    }
}
