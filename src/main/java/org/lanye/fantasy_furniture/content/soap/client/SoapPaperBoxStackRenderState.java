package org.lanye.fantasy_furniture.content.soap.client;

/** 包装盒摞体渲染 Pass：可见骨骼、层盒色与堆叠样式（线程局部）。 */
public final class SoapPaperBoxStackRenderState {

    private static final ThreadLocal<String> VISIBLE_BONE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> LAYER_MATERIAL = new ThreadLocal<>();
    private static final ThreadLocal<Integer> STACK_STYLE = new ThreadLocal<>();

    private SoapPaperBoxStackRenderState() {}

    public static void set(String visibleBone, int layerMaterial, int stackStyle) {
        VISIBLE_BONE.set(visibleBone);
        LAYER_MATERIAL.set(layerMaterial);
        STACK_STYLE.set(stackStyle);
    }

    public static String visibleBone() {
        return VISIBLE_BONE.get();
    }

    public static int layerMaterial() {
        Integer mat = LAYER_MATERIAL.get();
        return mat != null ? mat : 2;
    }

    public static int stackStyle() {
        Integer style = STACK_STYLE.get();
        return style != null ? style : 1;
    }

    public static void clear() {
        VISIBLE_BONE.remove();
        LAYER_MATERIAL.remove();
        STACK_STYLE.remove();
    }
}
