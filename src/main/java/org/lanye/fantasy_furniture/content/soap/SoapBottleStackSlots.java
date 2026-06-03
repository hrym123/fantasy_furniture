package org.lanye.fantasy_furniture.content.soap;

import java.util.Arrays;
import java.util.Set;

/**
 * 瓶罐堆叠 geo 陈列位：三种模型已对齐 {@code block1}…{@code block4} / {@code body_wash1}…{@code body_wash4}
 * 与自底向上摞层 index 0…3 的对应关系（与 {@code 洗发露_堆叠_x4} 等 bbmodel 一致）。
 */
public final class SoapBottleStackSlots {

    /** 沐浴露堆叠 geo 可见层骨骼（{@code body_wash_stack.geo.json}）。 */
    public static final String[] BODY_WASH_LAYER_BONES =
            {"body_wash1", "body_wash2", "body_wash3", "body_wash4"};

    /** 洗发露 / 乳霜混合陈列位骨骼（{@code block1}…{@code block4}）。 */
    public static final String[] BLOCK_LAYER_BONES = {"block1", "block2", "block3", "block4"};

    /** 乳霜纯摞堆叠 geo 全部陈列位（{@code block1}…{@code block5}，{@code 乳霜_堆叠_x5.bbmodel}）。 */
    public static final String[] BODY_CREAM_STACK_LAYER_BONES = {
        "block1", "block2", "block3", "block4", "block5"
    };

    private SoapBottleStackSlots() {}

    /** 自底向上第 {@code layerIndexFromBottom} 层（0 起）在 {@code kind} 堆叠 geo 中应点亮的骨骼名。 */
    public static String boneForLayer(SoapBottleKind kind, int layerIndexFromBottom) {
        return stackBoneForLayerIndex(kind, layerIndexFromBottom);
    }

    /** 陈列位 {@code slot}（1…4；乳霜纯摞可为 5）对应骨骼名。 */
    public static String boneForSlot(SoapBottleKind kind, int slot) {
        if (slot >= 1 && slot <= BLOCK_LAYER_BONES.length) {
            return switch (kind) {
                case BODY_WASH -> BODY_WASH_LAYER_BONES[slot - 1];
                case SHAMPOO, BODY_CREAM -> BLOCK_LAYER_BONES[slot - 1];
            };
        }
        if (kind == SoapBottleKind.BODY_CREAM && slot == 5) {
            return "block5";
        }
        return null;
    }

    public static String[] layerBones(SoapBottleKind kind) {
        return switch (kind) {
            case BODY_WASH -> BODY_WASH_LAYER_BONES;
            case SHAMPOO, BODY_CREAM -> BLOCK_LAYER_BONES;
        };
    }

    public static Set<String> layerBoneSet(SoapBottleKind kind) {
        return Set.copyOf(Arrays.asList(layerBones(kind)));
    }

    /** 乳霜堆叠 geo 中需参与显隐管理的骨骼（含 {@code block5} 顶位）。 */
    public static Set<String> bodyCreamStackBoneSet() {
        return Set.copyOf(Arrays.asList(BODY_CREAM_STACK_LAYER_BONES));
    }

    public static String stackBoneForLayerIndex(SoapBottleKind kind, int indexFromBottom) {
        String[] bones = layerBones(kind);
        if (indexFromBottom >= 0 && indexFromBottom < bones.length) {
            return bones[indexFromBottom];
        }
        if (kind == SoapBottleKind.BODY_CREAM && indexFromBottom < BODY_CREAM_STACK_LAYER_BONES.length) {
            return BODY_CREAM_STACK_LAYER_BONES[indexFromBottom];
        }
        return null;
    }
}
