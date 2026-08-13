package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackSlots;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.ShampooStackRenderState;

/** 混合瓶罐摞：按 (种类, 材质) 合并 Pass，同贴图多陈列位一次绘制。 */
public final class SoapBottleMixedStackRenderer {

    private final BodyWashStackLayerRenderer washStack = new BodyWashStackLayerRenderer();
    private final ShampooStackLayerRenderer shampooStack = new ShampooStackLayerRenderer();
    private final BodyCreamStackLayerRenderer creamStack = new BodyCreamStackLayerRenderer();

    public static boolean needsMixedPath(List<SoapBottleLayer> layers, SoapBottleKind hostKind) {
        return SoapBottleStackRules.needsPerLayerStackCollision(layers, hostKind);
    }

    public void render(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        boolean skipComboCreamSlot = SoapBottleStackRules.isCarrierCompleted(blockEntity.stackData());
        Map<PassKey, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < layers.size(); i++) {
            SoapBottleLayer layer = layers.get(i);
            int slot = i + 1;
            if (slot > SoapBottleKind.MIXED_MAX_STACK) {
                continue;
            }
            // 完成态第 3 位乳霜改由组合目录乳霜 geo 绘制
            if (skipComboCreamSlot
                    && i == 2
                    && layer.kind() == SoapBottleKind.BODY_CREAM) {
                continue;
            }
            groups
                    .computeIfAbsent(new PassKey(layer.kind(), layer.materialId()), k -> new ArrayList<>(2))
                    .add(i);
        }

        for (Map.Entry<PassKey, List<Integer>> entry : groups.entrySet()) {
            PassKey key = entry.getKey();
            Set<String> bones =
                    entry.getValue().stream()
                            .map(idx -> SoapBottleStackSlots.boneForLayer(key.kind(), idx))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            if (bones.isEmpty()) {
                continue;
            }
            renderMergedPass(
                    blockEntity,
                    key.kind(),
                    bones,
                    key.materialId(),
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay);
        }
    }

    /** 纯乳霜摞（≥2 层）：按 {@code block1}…{@code blockN} 绘制，可含第 5 陈列位。 */
    public void renderHomogeneousCreamStack(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        renderHomogeneousByMaterial(
                blockEntity,
                layers,
                SoapBottleKind.BODY_CREAM,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay);
    }

    /** 纯沐浴露 / 洗发露多瓶：按材质分桶，同贴图多陈列位一次绘制。 */
    public void renderHomogeneousKindStack(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            SoapBottleKind kind,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        renderHomogeneousByMaterial(
                blockEntity,
                layers,
                kind,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay);
    }

    private void renderHomogeneousByMaterial(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            SoapBottleKind kind,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        boolean skipComboCreamSlot =
                kind == SoapBottleKind.BODY_CREAM
                        && SoapBottleStackRules.isCarrierCompleted(blockEntity.stackData());
        Map<Integer, List<Integer>> byMaterial = new LinkedHashMap<>();
        for (int i = 0; i < layers.size(); i++) {
            if (skipComboCreamSlot && i == 2) {
                continue;
            }
            SoapBottleLayer layer = layers.get(i);
            byMaterial.computeIfAbsent(layer.materialId(), k -> new ArrayList<>(2)).add(i);
        }
        for (Map.Entry<Integer, List<Integer>> entry : byMaterial.entrySet()) {
            Set<String> bones =
                    entry.getValue().stream()
                            .map(idx -> SoapBottleStackSlots.boneForLayer(kind, idx))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            if (bones.isEmpty()) {
                continue;
            }
            renderMergedPass(
                    blockEntity,
                    kind,
                    bones,
                    entry.getKey(),
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay);
        }
    }

    private void renderMergedPass(
            SoapBottleBlockEntity blockEntity,
            SoapBottleKind kind,
            Set<String> bones,
            int materialId,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        switch (kind) {
            case BODY_WASH -> {
                BodyWashStackRenderState.setBones(bones, materialId);
                try {
                    washStack.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                } finally {
                    BodyWashStackRenderState.clear();
                }
            }
            case SHAMPOO -> {
                ShampooStackRenderState.setBones(bones, materialId);
                try {
                    shampooStack.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                } finally {
                    ShampooStackRenderState.clear();
                }
            }
            case BODY_CREAM -> {
                BodyCreamStackRenderState.setBones(bones, materialId);
                try {
                    creamStack.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                } finally {
                    BodyCreamStackRenderState.clear();
                }
            }
        }
    }

    private record PassKey(SoapBottleKind kind, int materialId) {}
}
