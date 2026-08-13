package org.lanye.fantasy_furniture.content.soap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
import org.lanye.fantasy_furniture.content.soap.SoapBottlePartPicks;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.reverie_core.composite.CompositePartId;
import org.lanye.reverie_core.composite.PartHitPicker;
import org.lanye.reverie_core.composite.PartPickEntry;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 瓶罐摞准心黑框：只描命中分件体素。 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SoapBottleCrosshairOutlineEvents {

    private SoapBottleCrosshairOutlineEvents() {}

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof BodyWashBlock)
                && !(state.getBlock() instanceof ShampooBlock)
                && !(state.getBlock() instanceof BodyCreamBlock)) {
            return;
        }
        if (!(mc.level.getBlockEntity(pos) instanceof SoapBottleBlockEntity be)) {
            return;
        }
        if (be.layerCount() <= 1 && !be.hasCarrier()) {
            return;
        }
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        List<PartPickEntry> entries = SoapBottlePartPicks.entries(be.stackData());
        CompositePartId hit = PartHitPicker.pick(bhr, facing, entries);
        if (hit == null) {
            return;
        }
        VoxelShape north = null;
        for (PartPickEntry entry : entries) {
            if (entry.partId().equals(hit)) {
                north = entry.northShape();
                break;
            }
        }
        if (north == null || north.isEmpty()) {
            return;
        }
        VoxelShape outline =
                VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
        event.setCanceled(true);
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
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
