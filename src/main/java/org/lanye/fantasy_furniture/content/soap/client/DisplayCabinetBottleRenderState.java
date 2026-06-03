package org.lanye.fantasy_furniture.content.soap.client;

import java.util.Collections;
import java.util.Set;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetBottleKind;

/** 陈列柜渲染 Pass：柜体 / 单瓶骨骼可见性与瓶罐贴图（线程局部）。 */
public final class DisplayCabinetBottleRenderState {

    public enum Pass {
        DEFAULT,
        /** 柜体 Pass：隐藏全部瓶罐骨骼。 */
        CABINET_ONLY,
        /** 瓶罐 Pass：仅显示 {@link #visibleBones()}。 */
        BOTTLE_ONLY
    }

    private static final ThreadLocal<Pass> PASS = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> VISIBLE_BONES = new ThreadLocal<>();
    private static final ThreadLocal<DisplayCabinetBottleKind> BOTTLE_KIND = new ThreadLocal<>();
    private static final ThreadLocal<Integer> BOTTLE_MATERIAL = new ThreadLocal<>();

    private DisplayCabinetBottleRenderState() {}

    public static void setCabinetOnly(Set<String> bottleBonesToHide) {
        PASS.set(Pass.CABINET_ONLY);
        VISIBLE_BONES.set(bottleBonesToHide);
        BOTTLE_KIND.remove();
        BOTTLE_MATERIAL.remove();
    }

    public static void setBottleOnly(
            Set<String> visibleBones, DisplayCabinetBottleKind kind, int materialId) {
        PASS.set(Pass.BOTTLE_ONLY);
        VISIBLE_BONES.set(visibleBones);
        BOTTLE_KIND.set(kind);
        BOTTLE_MATERIAL.set(materialId);
    }

    public static Pass pass() {
        Pass value = PASS.get();
        return value != null ? value : Pass.DEFAULT;
    }

    public static Set<String> managedBones() {
        Set<String> bones = VISIBLE_BONES.get();
        return bones != null ? bones : Collections.emptySet();
    }

    public static Set<String> visibleBones() {
        return managedBones();
    }

    public static DisplayCabinetBottleKind bottleKind() {
        return BOTTLE_KIND.get();
    }

    public static int bottleMaterial() {
        Integer mat = BOTTLE_MATERIAL.get();
        return mat != null ? mat : 1;
    }

    public static void clear() {
        PASS.remove();
        VISIBLE_BONES.remove();
        BOTTLE_KIND.remove();
        BOTTLE_MATERIAL.remove();
    }
}
