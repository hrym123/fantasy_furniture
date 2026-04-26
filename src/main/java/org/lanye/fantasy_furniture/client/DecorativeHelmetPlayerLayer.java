package org.lanye.fantasy_furniture.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.client.renderer.DecorativeHelmetGeoItemRenderer;
import org.lanye.fantasy_furniture.item.DecorativeHelmets;

/**
 * 在玩家头部骨骼上绘制所有 {@link org.lanye.fantasy_furniture.item.DecorativeHelmetItem} 的 geo。原版
 * {@link net.minecraft.client.renderer.entity.layers.CustomHeadLayer} 已由 Mixin 对这些物品取消，避免 JSON 物品模型与 geo 叠加。
 */
@OnlyIn(Dist.CLIENT)
public final class DecorativeHelmetPlayerLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public DecorativeHelmetPlayerLayer(PlayerRenderer parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!DecorativeHelmets.isDecorativeHelmet(headStack)) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        DecorativeHelmetGeoItemRenderer.INSTANCE.renderByItem(
                headStack,
                ItemDisplayContext.HEAD,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
