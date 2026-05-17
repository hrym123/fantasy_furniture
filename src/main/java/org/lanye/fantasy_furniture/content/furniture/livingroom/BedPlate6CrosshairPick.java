package org.lanye.fantasy_furniture.content.furniture.livingroom;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6ComponentPick;

/**
 * 床品准心解析（物理共通）：体素命中 + 防抖。客户端由 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.client.BedPlate6ClientPick}
 * 提供 {@link HitResult}，不依赖 Mixin。
 */
public final class BedPlate6CrosshairPick {

    /** 连续帧一致后才切换描边/解析子件，减轻体素边界抖动（T007）。 */
    private static final int OUTLINE_STABLE_TICKS = 4;

    private static ItemStack stableOutlinePick;
    private static ItemStack pendingOutlinePick;
    private static int pendingOutlineTicks;

    private BedPlate6CrosshairPick() {}

    public static ItemStack resolveClientPickForOutline(
            Level level, BlockState partState, net.minecraft.core.BlockPos partPos, @Nullable HitResult hit) {
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return ItemStack.EMPTY;
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (!hitState.is(ModBlocks.BED_PLATE6.block().get())) {
            return ItemStack.EMPTY;
        }
        if (!BedPlate6Block.bedFootWorldPos(partState, partPos)
                .equals(BedPlate6Block.bedFootWorldPos(hitState, bhr.getBlockPos()))) {
            return ItemStack.EMPTY;
        }
        net.minecraft.world.phys.Vec3 hitLoc = bhr.getLocation();
        if (level.isClientSide()) {
            hitLoc = org.lanye.fantasy_furniture.content.furniture.livingroom.client.BedPlate6ClientPick
                    .clipHitToDecorUnion(level, partState, partPos, bhr);
        }
        ItemStack raw =
                BedPlate6ComponentPick.stackForHit(
                        level, partState, partPos, hitLoc, bhr.getBlockPos());
        return stabilizeOutlinePick(raw);
    }

    public static ItemStack stabilizeOutlinePick(ItemStack raw) {
        if (raw == null || raw.isEmpty()) {
            stableOutlinePick = null;
            pendingOutlinePick = null;
            pendingOutlineTicks = 0;
            return ItemStack.EMPTY;
        }
        if (isBedPlateStack(raw)) {
            stableOutlinePick = null;
            pendingOutlinePick = null;
            pendingOutlineTicks = 0;
            return raw;
        }
        if (stableOutlinePick == null || isBedPlateStack(stableOutlinePick)) {
            stableOutlinePick = raw.copy();
            pendingOutlinePick = null;
            pendingOutlineTicks = 0;
            return stableOutlinePick;
        }
        if (stableOutlinePick != null && ItemStack.isSameItemSameTags(raw, stableOutlinePick)) {
            pendingOutlinePick = null;
            pendingOutlineTicks = 0;
            return stableOutlinePick;
        }
        if (pendingOutlinePick != null && ItemStack.isSameItemSameTags(raw, pendingOutlinePick)) {
            pendingOutlineTicks++;
            if (pendingOutlineTicks >= OUTLINE_STABLE_TICKS) {
                stableOutlinePick = pendingOutlinePick.copy();
                pendingOutlinePick = null;
                pendingOutlineTicks = 0;
                return stableOutlinePick;
            }
            return stableOutlinePick != null ? stableOutlinePick : raw;
        }
        pendingOutlinePick = raw.copy();
        pendingOutlineTicks = 1;
        return stableOutlinePick != null ? stableOutlinePick : raw;
    }

    private static boolean isBedPlateStack(ItemStack stack) {
        return stack.is(ModBlocks.BED_PLATE6.item().get());
    }
}
