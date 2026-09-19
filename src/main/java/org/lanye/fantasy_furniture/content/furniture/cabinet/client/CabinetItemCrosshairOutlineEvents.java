package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetModelOutlineShapes;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;
import org.lanye.reverie_core.composite.client.CompositeCrosshairOutlines;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 柜子准心：展品实测外接；否则按 geo 分件壳描边（细节贴模型）。不改共用描边风格。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CabinetItemCrosshairOutlineEvents {

    private CabinetItemCrosshairOutlineEvents() {}

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockHitResult bhr = event.getTarget();
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof CabinetBlock cabinet)) {
            return;
        }
        if (CabinetShelfClientPick.holdingDebugStick(mc.player)) {
            boolean includeAbsent = mc.player != null && mc.player.isShiftKeyDown();
            VoxelShape shelf =
                    CabinetShelfClientPick.aimedShelfShapeForDebug(mc.level, state, pos, bhr, includeAbsent);
            if (shelf != null && !shelf.isEmpty()) {
                return;
            }
        } else if (cabinet.kind() == CabinetKind.CABINET_1) {
            // 普通准心：柜子1仅两格之间的连接中隔可单独选中（柜子2勿描整层隔板）
            VoxelShape shelf = CabinetShelfClientPick.aimedShelfShape(mc.level, state, pos, bhr);
            if (shelf != null && !shelf.isEmpty()) {
                BlockPos origin = CabinetShelfClientPick.outlineOrigin(mc.level, state, pos, bhr, false);
                CompositeCrosshairOutlines.renderMultiCell(event, true, pos, shelf, origin, shelf);
                return;
            }
        }
        BlockPos master = CabinetBlock.masterPos(state, pos);
        VoxelShape outline;
        BlockPos origin;
        if (cabinet.kind() == CabinetKind.CABINET_2) {
            // 柜子2：准心始终描设计格（3×3），勿用整层隔板或邻格展品外接抢选
            outline = CabinetItemClientPick.aimedDesignCellShape(mc.level, state, pos, bhr);
            origin = master;
        } else {
            outline = CabinetItemClientPick.aimedItemShape(mc.level, state, pos, bhr);
            origin = CabinetItemClientPick.outlineOrigin(mc.level, state, pos, bhr);
        }
        if (outline == null || outline.isEmpty()) {
            outline = cabinetShellOutline(mc.level.getBlockEntity(master), cabinet.kind(), state);
            if (outline == null || outline.isEmpty()) {
                return;
            }
            CompositeCrosshairOutlines.renderMultiCell(event, false, pos, outline, pos, outline);
            return;
        }
        CompositeCrosshairOutlines.renderMultiCell(event, true, pos, outline, origin, outline);
    }

    private static VoxelShape cabinetShellOutline(BlockEntity raw, CabinetKind kind, BlockState state) {
        int mask = 0xF;
        if (raw instanceof CabinetBlockEntity be) {
            mask = be.shelvesMask();
        }
        CabinetSegment segment = CabinetSegment.ALONE;
        if (state.hasProperty(CabinetBlock.SEGMENT)) {
            segment = state.getValue(CabinetBlock.SEGMENT);
        }
        boolean openNeg =
                state.hasProperty(CabinetBlock.SIDE_OPEN_NEG) && state.getValue(CabinetBlock.SIDE_OPEN_NEG);
        boolean openPos =
                state.hasProperty(CabinetBlock.SIDE_OPEN_POS) && state.getValue(CabinetBlock.SIDE_OPEN_POS);
        VoxelShape north = CabinetModelOutlineShapes.northShell(kind, mask, segment, openNeg, openPos);
        Direction facing = state.getValue(CabinetBlock.FACING);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }
}