package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetYaw;

/**
 * 柜内展品：落到槽位中心 → 原地绕竖直轴偏航 → 绘制。
 *
 * <p>优先级：船/矿车/盔甲架等实体模型 → 普通方块网格 → {@link ItemDisplayContext#FIXED}。
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
            CabinetSlot slot,
            int itemYawSteps) {
        float fit = kind.fitSize(slot);

        poseStack.pushPose();
        poseStack.translate(0f, kind.itemCenterY(slot), kind.itemZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(CabinetYaw.degrees(itemYawSteps)));

        if (CabinetDisplayEntities.tryDraw(poseStack, bufferSource, light, stack, level, fit)) {
            poseStack.popPose();
            return;
        }
        if (tryDrawBlockModel(poseStack, bufferSource, light, stack, fit)) {
            poseStack.popPose();
            return;
        }

        drawFixedItem(poseStack, bufferSource, light, stack, level, fit);
        poseStack.popPose();
    }

    /**
     * @return true 已用方块模型画完
     */
    private static boolean tryDrawBlockModel(
            PoseStack poseStack, MultiBufferSource bufferSource, int light, ItemStack stack, float fit) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        BlockState state = blockItem.getBlock().defaultBlockState();
        // Geo / 实体动画方块无普通 baked block 网格，交给物品管线
        if (state.getRenderShape() != RenderShape.MODEL) {
            return false;
        }
        // 1×1×1 方块模型：缩到 fit 并居中到当前原点（槽位中心）
        poseStack.scale(fit, fit, fit);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(state, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY);
        return true;
    }

    private static void drawFixedItem(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            float fit) {
        float scale = fit / CabinetKind.FIXED_BLOCK_SCALE;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        poseStack.scale(scale, scale, scale);
        itemRenderer.render(
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                bufferSource,
                light,
                OverlayTexture.NO_OVERLAY,
                model);
    }
}
