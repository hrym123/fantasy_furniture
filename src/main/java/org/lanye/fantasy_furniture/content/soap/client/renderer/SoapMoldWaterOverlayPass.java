package org.lanye.fantasy_furniture.content.soap.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import org.lanye.fantasy_furniture.content.soap.client.SoapMoldDisplaySnapshot;

/**
 * C013 §5.4：BER 入队水面矩阵，于 {@code AFTER_TRANSLUCENT_BLOCKS} immediate flush（不写深度）。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SoapMoldWaterOverlayPass {

    private static final List<PendingWater> PENDING = new ArrayList<>();

    private SoapMoldWaterOverlayPass() {}

    static void enqueue(
            BlockPos blockPos,
            Matrix4f pose,
            Matrix3f normal,
            int light,
            SoapMoldDisplaySnapshot snapshot,
            float partialTick) {
        float wobble =
                snapshot.contents().phase() == SoapMoldPhase.CURING
                        ? (float)
                                Math.sin((snapshot.contents().cureFinishGameTime() + partialTick) * 0.05)
                                * 0.015f
                        : 0f;
        synchronized (PENDING) {
            PENDING.add(new PendingWater(blockPos, new Matrix4f(pose), new Matrix3f(normal), light, wobble));
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        List<PendingWater> batch;
        synchronized (PENDING) {
            if (PENDING.isEmpty()) {
                return;
            }
            batch = new ArrayList<>(PENDING);
            PENDING.clear();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (PendingWater pending : batch) {
            poseStack.pushPose();
            PoseStack.Pose pose = poseStack.last();
            pose.pose().set(pending.pose());
            pose.normal().set(pending.normal());

            SoapMoldWaterOverlayRenderer.render(
                    poseStack, bufferSource, pending.light(), pending.blockPos(), pending.surfaceWobble());
            poseStack.popPose();
        }

        bufferSource.endBatch(SoapMoldWaterRenderType.waterSurface());
    }

    private record PendingWater(
            BlockPos blockPos, Matrix4f pose, Matrix3f normal, int light, float surfaceWobble) {}
}
