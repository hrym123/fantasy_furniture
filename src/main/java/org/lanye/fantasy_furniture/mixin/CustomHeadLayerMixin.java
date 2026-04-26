package org.lanye.fantasy_furniture.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版 {@link CustomHeadLayer} 对头盔槽画的是物品模型（JSON/baked）；礼帽 geo 由 {@link org.lanye.fantasy_furniture.client.BlueTopHatPlayerLayer}
 * 调 BER 绘制。若两层都开，会叠出“物品模型 + 实体模型”。仅对玩家与本物品取消原版头层，避免非玩家实体戴帽时被取消却无 Layer 补画。
 */
@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fantasy_furniture$cancelPlayerDecorativeHatItemModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayer)) {
            return;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModItems.DECORATIVE_HELMET_BLUE_TOP_HAT.get())) {
            return;
        }
        ci.cancel();
    }
}
