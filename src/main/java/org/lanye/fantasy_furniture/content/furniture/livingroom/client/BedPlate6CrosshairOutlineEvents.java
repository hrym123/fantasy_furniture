package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/** 床板 6 准心黑框：{@link BedPlate6Block#getShape} 用并集做射线，此处只画当前子件（避免并集多框同亮）。 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BedPlate6CrosshairOutlineEvents {

    private BedPlate6CrosshairOutlineEvents() {}

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedPlate6Block)) {
            return;
        }
        BlockPos foot = BedPlate6Block.bedFootWorldPos(state, pos);
        var be = mc.level.getBlockEntity(foot);
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return;
        }

        VoxelShape mattressBase = BedPlate6Block.mattressBaseShape(state);
        VoxelShape outline =
                BedPlate6ClientPick.crosshairOutlinePieceShape(
                        mc.level,
                        state,
                        pos,
                        mattressBase,
                        plate,
                        state.getValue(BedBlock.FACING),
                        bhr);
        if (outline.isEmpty()) {
            return;
        }

        event.setCanceled(true);
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer =
                event.getMultiBufferSource().getBuffer(RenderType.lines());
        double ox = pos.getX() - cam.x;
        double oy = pos.getY() - cam.y;
        double oz = pos.getZ() - cam.z;
        float alpha = mc.player != null && mc.player.isSpectator() ? 1.0F : 0.4F;
        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        for (AABB box : outline.toAabbs()) {
            LevelRenderer.renderLineBox(poseStack, consumer, box, 0.0F, 0.0F, 0.0F, alpha);
        }
        poseStack.popPose();
    }
}
