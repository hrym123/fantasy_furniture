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
import org.lanye.fantasy_furniture.item.ModItems;

/**
 * 在玩家头部骨骼上绘制礼帽 geo（BER）。原版 {@link net.minecraft.client.renderer.entity.layers.CustomHeadLayer} 对本物品已由
 * {@link org.lanye.fantasy_furniture.mixin.CustomHeadLayerMixin} 取消，避免 JSON 物品模型与 geo 叠加。
 */
@OnlyIn(Dist.CLIENT)
public final class BlueTopHatPlayerLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public BlueTopHatPlayerLayer(PlayerRenderer parent) {
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
        if (!headStack.is(ModItems.DECORATIVE_HELMET_BLUE_TOP_HAT.get())) {
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
