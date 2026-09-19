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
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 床板1 准心黑框：只画当前选中的床体 / 床单 / 被套 / 单只枕头。 */
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
        VoxelShape outline = BedPlate1ClientPick.crosshairOutlinePieceShape(mc.level, state, pos, bhr);
        if (outline == null || outline.isEmpty()) {
            return;
        }
        CompositeCrosshairOutlines.renderPartOutline(event, pos, outline);
    }
}
