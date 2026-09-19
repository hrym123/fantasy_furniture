package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 床板 6 准心黑框：木架只描命中格；床单 / 枕头 / 被套描整件（床尾锚点）。 */
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
        VoxelShape piece = Shapes.empty();
        if (mc.level.getBlockEntity(foot) instanceof BedPlate6BlockEntity plate && plate.hasDuvet()) {
            piece =
                    BedPlate6ClientPick.crosshairOutlineComponentShape(
                            mc.level, state, pos, plate, state.getValue(BedPlate6Block.FACING), bhr);
        }
        CompositeCrosshairOutlines.renderMultiCell(
                event,
                !piece.isEmpty(),
                pos,
                BedPlate6Block.mattressBaseShape(state),
                foot,
                piece);
    }
}
