package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.bootstrap.effect.ModEffects;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.content.soap.effect.BubbleMobEffect;
import org.lanye.reverie_core.client.item.decorativehelmet.DecorativeHelmetArmorRenderer;
import org.lanye.reverie_core.item.DecorativeHelmetItem;

/**
 * 泡泡效果期头饰：不占头盔格，仅当实体有 {@code bubble} 效果时绘制。
 */
@OnlyIn(Dist.CLIENT)
public final class BubbleHeadLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private DecorativeHelmetArmorRenderer rabbitRenderer;
    private DecorativeHelmetArmorRenderer catRenderer;

    public BubbleHeadLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        MobEffectInstance bubble = entity.getEffect(ModEffects.BUBBLE.get());
        if (bubble == null) {
            return;
        }
        boolean cat = bubble.getAmplifier() == BubbleMobEffect.VARIANT_CAT;
        DecorativeHelmetItem item =
                (DecorativeHelmetItem) (cat ? ModItems.BUBBLE_HEAD_CAT.get() : ModItems.BUBBLE_HEAD_RABBIT.get());
        DecorativeHelmetArmorRenderer renderer = rendererFor(item, cat);
        ItemStack stack = new ItemStack(item);

        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);
        renderer.prepForRender(entity, stack, EquipmentSlot.HEAD, getParentModel());
        ResourceLocation texture = renderer.getTextureLocation(item);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        renderer.renderToBuffer(
                poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private DecorativeHelmetArmorRenderer rendererFor(DecorativeHelmetItem item, boolean cat) {
        if (cat) {
            if (catRenderer == null) {
                catRenderer = new DecorativeHelmetArmorRenderer(item);
            }
            return catRenderer;
        }
        if (rabbitRenderer == null) {
            rabbitRenderer = new DecorativeHelmetArmorRenderer(item);
        }
        return rabbitRenderer;
    }
}
