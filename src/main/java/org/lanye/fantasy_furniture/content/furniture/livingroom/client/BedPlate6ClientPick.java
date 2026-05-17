package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6CrosshairPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth.PickedDecorLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.reverie_core.util.VoxelShapeTranslation;

/** 床板 6 客户端准心 / 描边（读 {@link Minecraft#hitResult}，无 Mixin）。 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate6ClientPick {

    private BedPlate6ClientPick() {}

    /**
     * 将射线与床品并集体素求交，得到用于 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6ComponentPick}
     * 的命中点。描边仍仅用当前子件薄盒（T007 #6）；并集仅用于解析，使命中体素与 geo 盒一致（T008 H5）。
     */
    public static Vec3 clipHitToDecorUnion(
            Level level, BlockState state, BlockPos partPos, BlockHitResult bhr) {
        BlockPos foot = BedPlate6Block.bedFootWorldPos(state, partPos);
        var be = level.getBlockEntity(foot);
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return bhr.getLocation();
        }
        VoxelShape north = BedPlate6PickShapesNorth.unionNorthForPick(plate);
        if (north.isEmpty()) {
            return bhr.getLocation();
        }
        Direction facing = state.getValue(BedBlock.FACING);
        VoxelShape oriented = BedPlate6PickShapesNorth.orientForBedFacing(north, facing);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return bhr.getLocation();
        }
        Vec3 footOrigin = Vec3.atLowerCornerOf(foot);
        Vec3 eye = mc.player.getEyePosition(1.0F);
        Vec3 end = bhr.getLocation();
        Vec3 delta = end.subtract(eye);
        if (delta.lengthSqr() < 1.0E-8) {
            return bhr.getLocation();
        }
        Vec3 extended = eye.add(delta.normalize().scale(delta.length() + 0.5));
        Vec3 localStart = eye.subtract(footOrigin);
        Vec3 localEnd = extended.subtract(footOrigin);
        if (plate.getMediumPillowCount() == 2) {
            Vec3 dual =
                    BedPlate6PickShapesNorth.clipLocalDualMediumPreferFront(
                            facing, localStart, localEnd);
            if (dual != null) {
                return dual.add(footOrigin);
            }
        }
        Vec3 bestLocal = null;
        double bestDist2 = Double.MAX_VALUE;
        for (AABB box : oriented.toAabbs()) {
            Optional<Vec3> hit = box.clip(localStart, localEnd);
            if (hit.isPresent()) {
                double d2 = localStart.distanceToSqr(hit.get());
                if (d2 < bestDist2) {
                    bestDist2 = d2;
                    bestLocal = hit.get();
                }
            }
        }
        if (bestLocal == null) {
            return bhr.getLocation();
        }
        return bestLocal.add(footOrigin);
    }

    @Nullable
    public static HitResult currentCrosshairHit() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        return mc.hitResult;
    }

    public static ItemStack resolveCloneItemStack(Level level, BlockState state, BlockPos pos) {
        HitResult hit = currentCrosshairHit();
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return new ItemStack(ModBlocks.BED_PLATE6.item().get());
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (!(hitState.getBlock() instanceof BedPlate6Block)) {
            return new ItemStack(ModBlocks.BED_PLATE6.item().get());
        }
        if (!BedPlate6Block.bedFootWorldPos(state, pos)
                .equals(BedPlate6Block.bedFootWorldPos(hitState, bhr.getBlockPos()))) {
            return new ItemStack(ModBlocks.BED_PLATE6.item().get());
        }
        return BedPlate6CrosshairPick.resolveClientPickForOutline(level, state, pos, hit);
    }

    /**
     * 客户端玩家描边：每帧按当前准心即时解析单子件；不回退整床并集（避免多组件同亮）。
     */
    public static VoxelShape clientPlayerOutlineShape(
            Level level,
            BlockState state,
            BlockPos pos,
            VoxelShape base,
            BedPlate6BlockEntity plate,
            Direction facing) {
        HitResult hit = currentCrosshairHit();
        PickedDecorLayer layer = PickedDecorLayer.NONE;
        if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos foot = BedPlate6Block.bedFootWorldPos(state, pos);
            Vec3 hitLoc = bhr.getLocation();
            hitLoc = clipHitToDecorUnion(level, state, pos, bhr);
            layer = BedPlate6PickShapesNorth.pickLayerByVoxelHit(plate, hitLoc, foot, facing);
        }

        VoxelShape pieceLocal;
        if (layer == PickedDecorLayer.NONE) {
            pieceLocal = Shapes.empty();
        } else {
            VoxelShape pieceNorth = BedPlate6PickShapesNorth.northOutlinePieceNorth(plate, layer);
            if (pieceNorth == null || pieceNorth.isEmpty()) {
                pieceLocal = Shapes.empty();
            } else {
                pieceLocal = BedPlate6PickShapesNorth.orientForBedFacing(pieceNorth, facing);
            }
        }

        if (state.getValue(BedBlock.PART) != BedPart.FOOT) {
            double tx = -facing.getStepX();
            double ty = -facing.getStepY();
            double tz = -facing.getStepZ();
            VoxelShape inHead = VoxelShapeTranslation.translate(pieceLocal, tx, ty, tz);
            return pieceLocal.isEmpty() ? base : inHead;
        }
        return pieceLocal.isEmpty() ? base : pieceLocal;
    }
}
