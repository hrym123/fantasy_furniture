package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateBedFootPos;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.PickedLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.Plate;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 床板2/3/4 准心黑框：木架只描命中格；床单 / 被套 / 枕头描整件（床尾锚点）。 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BedPlateSimpleBeddingOutlineEvents {

    private BedPlateSimpleBeddingOutlineEvents() {}

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        Plate plate = BedPlateSimpleBeddingClientPick.plateOf(state);
        if (plate == null) {
            return;
        }
        BedPlateSimpleBeddingClientPick.BeddingView view =
                BedPlateSimpleBeddingClientPick.beddingAt(mc.level, state, pos);
        if (view == null || (!view.hasDuvet() && !view.hasCover() && !view.hasPillow())) {
            return;
        }
        PickedLayer layer = BedPlateSimpleBeddingClientPick.resolveLayer(plate, state, view, bhr);
        VoxelShape component =
                BedPlateSimpleBeddingShapes.outlineComponent(
                        plate,
                        state,
                        view.hasDuvet(),
                        view.hasCover(),
                        view.pillows(),
                        layer);
        CompositeCrosshairOutlines.renderMultiCell(
                event,
                layer != PickedLayer.BODY && !component.isEmpty(),
                pos,
                BedPlateSimpleBeddingShapes.bodyShape(plate, state),
                BedPlateBedFootPos.footPos(state, pos),
                component);
    }
}
