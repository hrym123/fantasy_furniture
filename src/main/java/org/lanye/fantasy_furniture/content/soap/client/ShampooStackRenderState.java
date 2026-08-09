package org.lanye.fantasy_furniture.content.soap.client;

import java.util.Collections;
import java.util.Set;
import org.lanye.fantasy_furniture.content.soap.ShampooMaterials;

/** 摞体渲染 Pass：可见骨骼名集合与颜料（线程局部）。 */
public final class ShampooStackRenderState {

    private static final ThreadLocal<Set<String>> VISIBLE_BONES = new ThreadLocal<>();
    private static final ThreadLocal<Integer> LAYER_MATERIAL = new ThreadLocal<>();

    private ShampooStackRenderState() {}

    public static void set(String visibleBone, int layerMaterial) {
        setBones(visibleBone == null ? Set.of() : Set.of(visibleBone), layerMaterial);
    }

    public static void setBones(Set<String> visibleBones, int layerMaterial) {
        VISIBLE_BONES.set(visibleBones == null || visibleBones.isEmpty() ? Set.of() : Set.copyOf(visibleBones));
        LAYER_MATERIAL.set(layerMaterial);
    }

    public static String visibleBone() {
        Set<String> bones = visibleBones();
        return bones.isEmpty() ? null : bones.iterator().next();
    }

    public static Set<String> visibleBones() {
        Set<String> bones = VISIBLE_BONES.get();
        return bones != null ? bones : Collections.emptySet();
    }

    public static int layerMaterial() {
        Integer mat = LAYER_MATERIAL.get();
        return mat != null ? mat : ShampooMaterials.DEFAULT;
    }

    public static void clear() {
        VISIBLE_BONES.remove();
        LAYER_MATERIAL.remove();
    }
}
