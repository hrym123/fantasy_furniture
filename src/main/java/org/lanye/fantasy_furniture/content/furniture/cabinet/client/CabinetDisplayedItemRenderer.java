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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetYaw;

/**
 * 柜内展品：层板底边正中 → 原地绕竖直轴偏航 → 绘制。
 *
 * <p>缩放按模型包围盒最大边对齐空腔约 {@link CabinetKind#INTERIOR_FIT}；贴底动态算。
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
            int slot,
            int itemYawSteps) {
        float fit = kind.fitSize(slot);

        poseStack.pushPose();
        poseStack.translate(kind.itemX(slot), kind.itemFloorY(slot), kind.itemZ());
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

    private static boolean tryDrawBlockModel(
            PoseStack poseStack, MultiBufferSource bufferSource, int light, ItemStack stack, float fit) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        BlockState state = blockItem.getBlock().defaultBlockState();
        if (state.getRenderShape() != RenderShape.MODEL) {
            return false;
        }
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        AABB bounds = CabinetModelBounds.fromModel(model, state);
        float scale = CabinetModelBounds.scaleToFit(bounds, fit);
        CabinetModelBounds.translateBlockToFloorCentered(poseStack, bounds, scale);
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
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        AABB afterFixed = CabinetModelBounds.afterDisplay(model, ItemDisplayContext.FIXED);
        float scale = CabinetModelBounds.scaleToFit(afterFixed, fit);
        CabinetModelBounds.translateToFloorCentered(poseStack, afterFixed, scale);
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
