package org.lanye.fantasy_furniture.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.content.furniture.common.item.DecorativeHelmets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版 {@link CustomHeadLayer} 对头盔槽画的是物品模型（JSON/baked）；装饰头饰的 geo 由 {@link org.lanye.fantasy_furniture.content.furniture.common.client.DecorativeHelmetPlayerLayer}
 * 绘制。仅对玩家且头盔为 {@link org.lanye.fantasy_furniture.content.furniture.common.item.DecorativeHelmetItem} 时取消原版头层。
 */
@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fantasy_furniture$cancelPlayerDecorativeHelmetItemModel(
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
        if (!DecorativeHelmets.isDecorativeHelmet(head)) {
            return;
        }
        ci.cancel();
    }
}
