package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.Cabinet1OpenColumn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 柜子展品客户端准心 / 中键（读 {@link Minecraft#hitResult}）。 */
@OnlyIn(Dist.CLIENT)
public final class CabinetItemClientPick {

    private CabinetItemClientPick() {}

    public static ItemStack resolveCloneItemStack(Level level, BlockState state, BlockPos pos) {
        ItemStack cabinet = new ItemStack(state.getBlock().asItem());
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return cabinet;
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (!(hitState.getBlock() instanceof CabinetBlock)) {
            return cabinet;
        }
        Cabinet1OpenColumn.Aimed aimed = aimedExhibit(level, hitState, bhr.getBlockPos(), bhr);
        if (aimed == null) {
            return cabinet;
        }
        ItemStack displayed = aimed.be().getItem(aimed.slot());
        return displayed.isEmpty() ? cabinet : displayed.copy();
    }

    /**
     * @return 对准展品的体素（已按柜朝向旋转，相对展品所在格底）；未命中为 null
     */
    @Nullable
    public static VoxelShape aimedItemShape(Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr) {
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return null;
        }
        Cabinet1OpenColumn.Aimed aimed = aimedExhibit(level, state, hitPos, bhr);
        if (aimed == null) {
            return null;
        }
        AABB local = displayNorthLocal(aimed.be(), aimed.slot());
        if (local == null) {
            return null;
        }
        Direction facing = state.getValue(CabinetBlock.FACING);
        return CabinetItemPicks.orientedOutline(local, facing);
    }

    /** 柜子2：准心所对设计格描边（非隔板横条）。 */
    @Nullable
    public static VoxelShape aimedDesignCellShape(
            Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr) {
        if (!(state.getBlock() instanceof CabinetBlock cabinet) || cabinet.kind() != CabinetKind.CABINET_2) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return null;
        }
        BlockPos master = CabinetBlock.masterPos(state, hitPos);
        int slot =
                cabinet.kind()
                        .slotFromHit(
                                master,
                                state.getValue(CabinetBlock.FACING),
                                bhr,
                                player.getEyePosition(1.0f),
                                player.getViewVector(1.0f));
        if (slot < 0) {
            return null;
        }
        VoxelShape north = cabinet.kind().designCellNorthShape(slot);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                north, state.getValue(CabinetBlock.FACING));
    }

    public static BlockPos outlineOrigin(Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr) {
        Cabinet1OpenColumn.Aimed aimed = aimedExhibit(level, state, hitPos, bhr);
        if (aimed != null) {
            return aimed.be().getBlockPos();
        }
        return CabinetBlock.masterPos(state, hitPos);
    }

    /** @deprecated 使用 {@link #outlineOrigin(Level, BlockState, BlockPos, BlockHitResult)} */
    @Deprecated
    public static BlockPos outlineOrigin(BlockState state, BlockPos hitPos) {
        return CabinetBlock.masterPos(state, hitPos);
    }

    @Nullable
    static Cabinet1OpenColumn.Aimed aimedExhibit(
            Level level, BlockState state, BlockPos hitPos, @Nullable BlockHitResult bhr) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !(state.getBlock() instanceof CabinetBlock cabinet)) {
            return null;
        }
        Direction facing = state.getValue(CabinetBlock.FACING);
        if (cabinet.kind() == CabinetKind.CABINET_1) {
            // 客户端用实测盒；柱内逐格求最近
            var cells = Cabinet1OpenColumn.cellsBottomToTop(level, hitPos);
            Cabinet1OpenColumn.Aimed best = null;
            double bestDist = Double.MAX_VALUE;
            Vec3 eyeWorld = player.getEyePosition(1.0f);
            Vec3 lookWorld = player.getViewVector(1.0f);
            for (CabinetBlockEntity cell : cells) {
                int slot =
                        CabinetItemPicks.pickNearestOccupied(
                                cell,
                                facing,
                                eyeWorld,
                                lookWorld,
                                s -> displayNorthLocal(cell, s));
                if (slot < 0) {
                    continue;
                }
                AABB box = displayNorthLocal(cell, slot);
                if (box == null) {
                    continue;
                }
                Vec3 eye = CabinetKind.toNorthLocal(cell.getBlockPos(), facing, eyeWorld);
                Vec3 look = CabinetKind.lookToNorth(facing, lookWorld);
                double lenSq = look.lengthSqr();
                if (lenSq < 1.0e-8) {
                    continue;
                }
                Vec3 dir = look.scale(1.0 / Math.sqrt(lenSq));
                var pt = box.clip(eye, eye.add(dir.scale(12.0)));
                if (pt.isEmpty()) {
                    continue;
                }
                double d = eye.distanceToSqr(pt.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = new Cabinet1OpenColumn.Aimed(cell, slot);
                }
            }
            return best;
        }
        BlockPos master = CabinetBlock.masterPos(state, hitPos);
        BlockEntity raw = level.getBlockEntity(master);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return null;
        }
        int slot =
                CabinetItemPicks.pickNearestOccupied(
                        be,
                        facing,
                        player.getEyePosition(1.0f),
                        player.getViewVector(1.0f),
                        s -> displayNorthLocal(be, s));
        return slot < 0 ? null : new Cabinet1OpenColumn.Aimed(be, slot);
    }

    @Nullable
    static AABB displayNorthLocal(CabinetBlockEntity be, int slot) {
        ItemStack stack = be.getItem(slot);
        Level level = be.getLevel();
        if (stack.isEmpty() || level == null) {
            return null;
        }
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        float floorY = CabinetStackLayout.floorY(be, i);
        CabinetKind.CavityFit cavity = CabinetStackLayout.cavityFitStacked(be, i);
        CabinetItemPicks.Size size = CabinetDisplayedItemRenderer.measureRenderedSize(
                stack, level, cavity.width(), cavity.height(), cavity.depth());
        if (size.height() <= 1.0e-4f) {
            return null;
        }
        return CabinetItemPicks.northLocalAabb(kind, i, floorY, size.atLeast(CabinetItemPicks.MIN_EXTENT), be.itemYaw(i));
    }
}
