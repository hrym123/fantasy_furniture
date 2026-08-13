package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 瓶罐摞准心黑框：只描命中分件体素（描边实现见 core）。 */
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
        CompositeCrosshairOutlines.resolveAndRender(
                event, facing, SoapBottlePartPicks.entries(be.stackData()), true);
    }
}
