package org.lanye.fantasy_furniture.content.furniture.common.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.reverie_core.seat.entity.ReverieSeatEntity;

/** 家具坐骑不可见，无需绘制。 */
@OnlyIn(Dist.CLIENT)
public final class FurnitureSeatRenderer extends EntityRenderer<ReverieSeatEntity> {

    private static final ResourceLocation DUMMY =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/stone.png");

    public FurnitureSeatRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(
            ReverieSeatEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        // no-op
    }

    @Override
    public ResourceLocation getTextureLocation(ReverieSeatEntity entity) {
        return DUMMY;
    }
}
