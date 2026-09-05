package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetYaw;

/**
 * 柜内展品：落到槽位中心 → 原地绕竖直轴偏航 → 缩放 → {@link ItemDisplayContext#FIXED}。
 *
 * <p>转轴过槽位中心（垂直地面），不绕柜心公转；与展示框「先到位再转」同序。
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
        float scale = fit / CabinetKind.FIXED_BLOCK_SCALE;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);

        poseStack.pushPose();
        // 1) 槽位中心（柜体北向局部）
        poseStack.translate(0f, kind.itemCenterY(slot), kind.itemZ());
        // 2) 原地绕竖直轴（过槽位中心）
        poseStack.mulPose(Axis.YP.rotationDegrees(CabinetYaw.degrees(itemYawSteps)));
        // 3) 统一外接缩放；再交给 FIXED（物品 JSON display.fixed 叠在本地原点）
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
        poseStack.popPose();
    }
}
