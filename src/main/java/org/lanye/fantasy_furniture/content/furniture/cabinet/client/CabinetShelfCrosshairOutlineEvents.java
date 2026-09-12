package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 持幻想调试棒瞄准柜子隔板时，高亮该隔板而非整柜。 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CabinetShelfCrosshairOutlineEvents {

    private CabinetShelfCrosshairOutlineEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !CabinetShelfClientPick.holdingDebugStick(mc.player)) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return;
        }
        boolean includeAbsent = mc.player != null && mc.player.isShiftKeyDown();
        VoxelShape outline =
                CabinetShelfClientPick.aimedShelfShapeForDebug(mc.level, state, pos, bhr, includeAbsent);
        if (outline == null || outline.isEmpty()) {
            return;
        }
        BlockPos origin = CabinetShelfClientPick.outlineOrigin(mc.level, state, pos, bhr, includeAbsent);
        CompositeCrosshairOutlines.renderPartOutline(event, origin, outline);
    }
}
