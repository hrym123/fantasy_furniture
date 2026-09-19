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
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1CollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1CollisionShapes.PickedLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 床板1 准心黑框：木架只描命中格；床单 / 被套 / 枕头描整件（床尾右锚点）。 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BedPlate1CrosshairOutlineEvents {

    private BedPlate1CrosshairOutlineEvents() {}

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedPlate1Block)) {
            return;
        }
        BedPlate1BlockEntity plate = BedPlate1Block.decorEntity(mc.level, state, pos);
        if (plate == null
                || (!plate.hasDuvet() && !plate.hasCover() && !plate.sheetPillows().hasAny())) {
            return;
        }
        PickedLayer layer = BedPlate1ClientPick.resolveLayer(mc.level, state, bhr);
        VoxelShape component =
                BedPlate1CollisionShapes.outlineComponent(
                        state, plate.hasDuvet(), plate.hasCover(), plate.sheetPillows(), layer);
        CompositeCrosshairOutlines.renderMultiCell(
                event,
                layer != PickedLayer.BODY && !component.isEmpty(),
                pos,
                BedPlate1CollisionShapes.bodyShape(state),
                BedPlate1Block.renderAnchorPos(state, pos),
                component);
    }
}
