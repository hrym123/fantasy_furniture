package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;

/**
 * 柜内展品（C020 稳妥写法）：平移到槽位中心 → 统一缩放 → {@link ItemDisplayContext#FIXED}。
 *
 * <p>假定当前 PoseStack 已处在「北向模型空间」（与柜体 Geo 同一套 translate+rotateBlock）。
 */
@OnlyIn(Dist.CLIENT)
final class CabinetDisplayedItemRenderer {

    private CabinetDisplayedItemRenderer() {}

    static void draw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            CabinetKind kind,
            CabinetSlot slot) {
        float fit = kind.fitSize(slot);
        // FIXED 对方块约再 ×0.5，此处补偿，使视觉边长 ≈ fit（空腔 80%）
        float scale = fit / CabinetKind.FIXED_BLOCK_SCALE;

        poseStack.pushPose();
        poseStack.translate(0f, kind.itemCenterY(slot), kind.itemZ());
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        level,
                        0);
        poseStack.popPose();
    }
}
