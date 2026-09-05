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
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.Plate;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 床板2/3/4 准心黑框：有床单时只画当前选中层。 */
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
        VoxelShape outline =
                BedPlateSimpleBeddingClientPick.crosshairOutlinePieceShape(mc.level, state, pos, bhr);
        if (outline == null || outline.isEmpty()) {
            return;
        }
        CompositeCrosshairOutlines.renderPartOutline(event, pos, outline);
    }
}
