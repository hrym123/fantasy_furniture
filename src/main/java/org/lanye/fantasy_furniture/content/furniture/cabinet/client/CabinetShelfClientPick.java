package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetJointShelfPick;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.reverie_core.content.fantasy_core.item.FantasyDebugStickItem;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 柜子隔板准心拾取 / 描边形。普通准心柜子1仅连接中隔；调试棒可点选本段全部隔板。 */
@OnlyIn(Dist.CLIENT)
public final class CabinetShelfClientPick {

    private CabinetShelfClientPick() {}

    public static boolean holdingDebugStick(@Nullable Player player) {
        return player != null
                && player.getMainHandItem().getItem() instanceof FantasyDebugStickItem;
    }

    @Nullable
    public static VoxelShape aimedShelfShape(Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr) {
        return shapeFor(aimedHit(level, state, hitPos, bhr, false), state);
    }

    @Nullable
    public static VoxelShape aimedShelfShapeForDebug(
            Level level, BlockState state, BlockPos hitPos, BlockHitResult bhr, boolean includeAbsent) {
        return shapeFor(aimedDebugHit(level, state, hitPos, bhr, includeAbsent), state);
    }

    @Nullable
    private static VoxelShape shapeFor(@Nullable CabinetJointShelfPick.Hit hit, BlockState state) {
        if (hit == null || !(state.getBlock() instanceof CabinetBlock cabinet)) {
            return null;
        }
        CabinetKind kind = cabinet.kind();
        Direction facing = hit.facing();
        VoxelShape north = kind.shelfNorthShape(hit.shelf());
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }

    @Nullable
    public static CabinetJointShelfPick.Hit aimedHit(
            Level level, BlockState state, BlockPos hitPos, @Nullable BlockHitResult bhr, boolean includeAbsent) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return null;
        }
        return CabinetJointShelfPick.pick(
                level,
                hitPos,
                state,
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f),
                bhr != null ? bhr.getLocation() : null,
                includeAbsent);
    }

    @Nullable
    public static CabinetJointShelfPick.Hit aimedDebugHit(
            Level level, BlockState state, BlockPos hitPos, @Nullable BlockHitResult bhr, boolean includeAbsent) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return null;
        }
        return CabinetJointShelfPick.pickAnyShelf(
                level,
                hitPos,
                state,
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f),
                bhr != null ? bhr.getLocation() : null,
                includeAbsent);
    }

    public static BlockPos outlineOrigin(
            Level level, BlockState state, BlockPos hitPos, @Nullable BlockHitResult bhr, boolean includeAbsent) {
        CabinetJointShelfPick.Hit hit =
                holdingDebugStick(Minecraft.getInstance().player)
                        ? aimedDebugHit(level, state, hitPos, bhr, includeAbsent)
                        : aimedHit(level, state, hitPos, bhr, includeAbsent);
        if (hit != null) {
            return hit.ownerPos();
        }
        return CabinetBlock.masterPos(state, hitPos);
    }
}
