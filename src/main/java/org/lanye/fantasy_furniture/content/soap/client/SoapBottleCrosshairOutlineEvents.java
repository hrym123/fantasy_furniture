package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
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
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;

/** 瓶罐摞 / 单瓶准心黑框：统一走 core 整形 forAllEdges（含 optimize）。 */
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
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        // 多件：只描命中分件；单件无载体：描整块 getShape（optimize 去多盒接缝）
        if (be.layerCount() > 1 || be.hasCarrier()) {
            CompositeCrosshairOutlines.resolveAndRender(
                    event, facing, SoapBottlePartPicks.entries(be.stackData()), true);
            return;
        }
        VoxelShape shape =
                state.getShape(mc.level, pos, CollisionContext.of(mc.player));
        CompositeCrosshairOutlines.renderPartOutline(event, pos, shape);
    }
}
