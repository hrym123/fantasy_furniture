package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackSlots;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashStackRenderState;
import org.lanye.fantasy_furniture.content.soap.client.ShampooStackRenderState;

/** 混合瓶罐摞：每层按种类选用对应 geo 与堆叠陈列位骨骼（最多 4 位，不含乳霜 ×5 顶位）。 */
public final class SoapBottleMixedStackRenderer {

    private final BodyWashStackLayerRenderer washStack = new BodyWashStackLayerRenderer();
    private final ShampooStackLayerRenderer shampooStack = new ShampooStackLayerRenderer();
    private final BodyCreamStackLayerRenderer creamStack = new BodyCreamStackLayerRenderer();

    public static boolean needsMixedPath(List<SoapBottleLayer> layers, SoapBottleKind hostKind) {
        if (layers.isEmpty()) {
            return false;
        }
        if (SoapBottleStackRules.isMixed(layers)) {
            return true;
        }
        if (layers.size() == 1) {
            return layers.get(0).kind() != hostKind;
        }
        return layers.get(0).kind() != hostKind;
    }

    public void render(
            SoapBottleBlockEntity blockEntity,
            List<SoapBottleLayer> layers,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        for (int i = 0; i < layers.size(); i++) {
            SoapBottleLayer layer = layers.get(i);
            int slot = i + 1;
            if (slot > SoapBottleKind.MIXED_MAX_STACK) {
                continue;
            }
            renderStackLayer(
                    blockEntity,
                    layer,
                    SoapBottleStackSlots.boneForLayer(layer.kind(), i),
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
        for (int i = 0; i < layers.size(); i++) {
            SoapBottleLayer layer = layers.get(i);
            String bone = SoapBottleStackSlots.boneForLayer(SoapBottleKind.BODY_CREAM, i);
            BodyCreamStackRenderState.set(bone, layer.materialId());
            try {
                creamStack.render(
                        blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            } finally {
                BodyCreamStackRenderState.clear();
            }
        }
    }

    private void renderStackLayer(
            SoapBottleBlockEntity blockEntity,
            SoapBottleLayer layer,
            String bone,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        switch (layer.kind()) {
            case BODY_WASH -> {
                BodyWashStackRenderState.set(bone, layer.materialId());
                try {
                    washStack.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                } finally {
                    BodyWashStackRenderState.clear();
                }
            }
            case SHAMPOO -> {
                ShampooStackRenderState.set(bone, layer.materialId());
                try {
                    shampooStack.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                } finally {
                    ShampooStackRenderState.clear();
                }
            }
            case BODY_CREAM -> {
                BodyCreamStackRenderState.set(bone, layer.materialId());
                try {
                    creamStack.render(
                            blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                } finally {
                    BodyCreamStackRenderState.clear();
                }
            }
        }
    }
}
