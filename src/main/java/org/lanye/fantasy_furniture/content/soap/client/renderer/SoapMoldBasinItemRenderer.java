package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.client.SoapMoldDisplaySnapshot;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;

/** 肥皂模具盆内原料 Item 绘制。 */
@OnlyIn(Dist.CLIENT)
final class SoapMoldBasinItemRenderer {

    private static final float ITEM_SCALE = 0.20f;

    private static final float BASIN_CENTER_X = 0f;
    private static final float BASIN_CENTER_Z = 1.5f;

    private SoapMoldBasinItemRenderer() {}

    static void draw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            String anchorBone,
            SoapMoldDisplaySnapshot snapshot) {
        if (stack.isEmpty()) {
            return;
        }

        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        poseStack.translate(0.0, 0.5f, 0.0);
        applyIngredientLeanBack(poseStack, anchorBone);

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        Minecraft.getInstance().level,
                        0);
    }

    private static void applyIngredientLeanBack(PoseStack poseStack, String anchorBone) {
        float px;
        float pz;
        switch (anchorBone) {
            case "ingredient_liquid" -> {
                px = 2.0f;
                pz = 0.4f;
            }
            case "ingredient_honey" -> {
                px = -2.0f;
                pz = 0.4f;
            }
            case "ingredient_pigment" -> {
                px = 0f;
                pz = 2.6f;
            }
            default -> {
                return;
            }
        }
        float dx = px - BASIN_CENTER_X;
        float dz = pz - BASIN_CENTER_Z;
        float awayYaw = (float) Math.toDegrees(Math.atan2(dx, -dz));
        float dist = Mth.sqrt(dx * dx + dz * dz);
        float pitch = -(12f + dist * 2.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(awayYaw * 0.3f));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }
}
