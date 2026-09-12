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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetItemPicks;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;

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
        BlockPos hitMaster = CabinetBlock.masterPos(hitState, bhr.getBlockPos());
        BlockPos selfMaster = CabinetBlock.masterPos(state, pos);
        if (!hitMaster.equals(selfMaster)) {
            return cabinet;
        }
        int slot = aimedOccupiedSlot(level, hitState, hitMaster, bhr);
        if (slot < 0) {
            return cabinet;
        }
        BlockEntity raw = level.getBlockEntity(hitMaster);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return cabinet;
        }
        ItemStack displayed = be.getItem(slot);
        return displayed.isEmpty() ? cabinet : displayed.copy();
    }

    /**
     * @return 对准展品的体素（已按柜朝向旋转，相对 master 底格）；未命中为 null
     */
    @Nullable
    public static VoxelShape aimedItemShape(Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr) {
        if (!(state.getBlock() instanceof CabinetBlock)) {
            return null;
        }
        BlockPos master = CabinetBlock.masterPos(state, hitPos);
        BlockEntity raw = level.getBlockEntity(master);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return null;
        }
        int slot = aimedOccupiedSlot(level, state, master, bhr);
        if (slot < 0) {
            return null;
        }
        AABB local = displayNorthLocal(be, slot);
        if (local == null) {
            return null;
        }
        Direction facing = state.getValue(CabinetBlock.FACING);
        return CabinetItemPicks.orientedOutline(local, facing);
    }


    public static BlockPos outlineOrigin(BlockState state, BlockPos hitPos) {
        return CabinetBlock.masterPos(state, hitPos);
    }

    static int aimedOccupiedSlot(Level level, BlockState state, BlockPos master, BlockHitResult bhr) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return -1;
        }
        BlockEntity raw = level.getBlockEntity(master);
        if (!(raw instanceof CabinetBlockEntity be)) {
            return -1;
        }
        Direction facing = state.getValue(CabinetBlock.FACING);
        return CabinetItemPicks.pickNearestOccupied(
                be,
                facing,
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f),
                slot -> displayNorthLocal(be, slot));
    }

    @Nullable
    static AABB displayNorthLocal(CabinetBlockEntity be, int slot) {
        ItemStack stack = be.getItem(slot);
        Level level = be.getLevel();
        if (stack.isEmpty() || level == null) {
            return null;
        }
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, kind.slotCount());
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
