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
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackData;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackSlots;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.ShampooStackRenderState;

/** 混合 / 含空槽瓶罐摞：按槽号点亮骨骼，同贴图多陈列位合并 Pass。 */
public final class SoapBottleMixedStackRenderer {

    private final BodyWashStackLayerRenderer washStack = new BodyWashStackLayerRenderer();
    private final ShampooStackLayerRenderer shampooStack = new ShampooStackLayerRenderer();
    private final BodyCreamStackLayerRenderer creamStack = new BodyCreamStackLayerRenderer();

    public static boolean needsMixedPath(SoapBottleStackData data, SoapBottleKind hostKind) {
        return SoapBottleStackRules.needsPerLayerStackCollision(data, hostKind);
    }

    /** @deprecated 请用 {@link #needsMixedPath(SoapBottleStackData, SoapBottleKind)} */
    @Deprecated
    public static boolean needsMixedPath(List<SoapBottleLayer> layers, SoapBottleKind hostKind) {
        return SoapBottleStackRules.needsPerLayerStackCollision(layers, hostKind);
    }

    public void renderFromSlots(
            SoapBottleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        SoapBottleStackData data = blockEntity.stackData();
        boolean skipComboCreamSlot = SoapBottleStackRules.isCarrierCompleted(data);
        Map<PassKey, List<Integer>> groups = new LinkedHashMap<>();
        for (int slotIdx = 0; slotIdx < SoapBottleStackData.MAX_SLOTS; slotIdx++) {
            SoapBottleLayer layer = data.slotAt(slotIdx);
            if (layer == null) {
                continue;
            }
            if (skipComboCreamSlot
                    && slotIdx == 2
                    && layer.kind() == SoapBottleKind.BODY_CREAM) {
                continue;
            }
            groups
                    .computeIfAbsent(new PassKey(layer.kind(), layer.materialId()), k -> new ArrayList<>(2))
                    .add(slotIdx);
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

    public void render(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        renderFromSlots(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    public void renderHomogeneousCreamStack(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        renderFromSlots(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    public void renderHomogeneousKindStack(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            SoapBottleKind kind,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        renderFromSlots(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
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
